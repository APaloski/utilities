package com.paloski.statistics.concurrent;

import com.paloski.statistics.Statistics;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.processing.Completion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class StatisticsRecordingCompletionServiceTest {

	public static final String DATA_POINTS__NUMBER_OF_THREADS = "Data-Points::Number-of-Threads";
	public static final String DATA_POINTS__NUMBER_OF_SUCCESSES = "Data-Points::Number-of-Successes";
	public static final String DATA_POINTS__NUMBER_OF_ERRORS = "Data-Points::Number-of-Errors";
	public static final String DATA_POINTS__AWAIT_AMOUNT = "Data-Points::Await-Amount";

	@DataPoints(DATA_POINTS__NUMBER_OF_THREADS)
	public static int[] getNumberOfThreads() {
		return new int[] { 1, 2, 5, 10, 20, 30 };
	}

	@DataPoints(DATA_POINTS__NUMBER_OF_SUCCESSES)
	public static int[] getNumberOfSuccesses() {
		return new int[] { 1, 50, 300, 500_000, 1_000_000 };
	}

	@DataPoints(DATA_POINTS__NUMBER_OF_ERRORS)
	public static int[] getNumberOfErrors() {
		return new int[] { 1, 50, 300, 500_000, 1_000_000 };
	}

	@DataPoints(DATA_POINTS__AWAIT_AMOUNT)
	public static long[] getAwaitAmount() {
		return new long[] { 1L, 3L, 5L, 9L, 13L, 21L };
	}

	@Mock
	public CompletionService<Void> mockCompletionService;

	@Mock
	public Callable<Void> mockCallable;

	@Mock
	public Runnable mockRunnable;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		when(mockCompletionService.submit(any(Callable.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				((Callable) invocation.getArguments()[0]).call();
				return null;
			}
		});
		when(mockCompletionService.submit(any(Runnable.class), any(Void.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			}
		});
	}

	@Theory
	public void statRecording_successHasExpectedCount(@FromDataPoints(DATA_POINTS__NUMBER_OF_THREADS) final int threadCount,
													  @FromDataPoints(DATA_POINTS__NUMBER_OF_SUCCESSES) final int successCount,
													  @FromDataPoints(DATA_POINTS__NUMBER_OF_ERRORS) final int errCount) throws InterruptedException {
		System.out.format("Trying: thread count: %d success count: %d error count: %d\n", threadCount, successCount, errCount);
		final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(new ExecutorCompletionService<Void>(executor));

		for(int x = 0; x < successCount; x++) {
			service.submit(new SuccessCallable());
		}

		for(int x = 0; x < errCount; x++) {
			service.submit(new ErrorCallable());
		}

		executor.shutdown();
		if(!executor.awaitTermination(1, TimeUnit.MINUTES)) {
			fail("Executor did not shut down properly");
		}

		final Statistics stats = service.getStatistics();
		assertThat(stats.getSuccessCount()).isEqualTo(successCount);
		assertThat(stats.getErrorCount()).isEqualTo(errCount);
	}
	
	@Theory
	public void submit_callable_invokesCallableUsingDelegateService() throws Exception {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		service.submit(mockCallable);
		verify(mockCompletionService).submit(any(Callable.class));
		verify(mockCallable).call();
	}
	
	@Theory
	public void submit_runnable_invokesRunnableUsingDelegateService() {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		service.submit(mockRunnable, null);
		verify(mockCompletionService).submit(any(Callable.class));
		verify(mockRunnable).run();
	}
	
	@Theory
	public void take_invokesDelegateTake() throws InterruptedException {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		service.take();
		verify(mockCompletionService).take();
	}

	@Theory
	public void poll_noArg_invokesDelegatePoll() {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		service.poll();
		verify(mockCompletionService).poll();
	}

	@Theory
	public void poll_timeout_invokesDelegatePoll(final TimeUnit timeUnit,
												 @FromDataPoints(DATA_POINTS__AWAIT_AMOUNT) final long awaitAmount) throws InterruptedException {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		service.poll(awaitAmount, timeUnit);
		verify(mockCompletionService).poll(eq(awaitAmount), eq(timeUnit));
	}

	@Theory
	public void constructor_nullArgumentThrowsNPE() {
		expected.expect(NullPointerException.class);
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(null);
	}

	@Theory
	public void submit_callable_nullCallableFailsFast() {
		final StatisticsRecordingCompletionService<Void> service = new StatisticsRecordingCompletionService<>(mockCompletionService);
		expected.expect(NullPointerException.class);
		service.submit(null);
	}

	private static class SuccessCallable implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			return null;
		}
	}

	public static class ErrorCallable implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			throw new Exception("Test");
		}
	}



}
