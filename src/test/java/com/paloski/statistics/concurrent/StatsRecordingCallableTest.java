package com.paloski.statistics.concurrent;


import com.paloski.statistics.Statistics;
import com.paloski.statistics.StatisticsRecorder;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.InvalidMarkException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class StatsRecordingCallableTest {

	@DataPoints
	public static List<String> getExpectedResults() {
		return Arrays.asList("TEST1", "TEST2", "", null);
	}

	@DataPoints
	public static List<Exception> getExpectedExceptions() {
		return Arrays.asList(new IOException(), new RuntimeException(), new Exception());
	}

	@DataPoints
	public static List<RuntimeException> getExpectedRuntimeExceptions() {
		return Arrays.asList(new IllegalArgumentException(), new RuntimeException(), new InvalidMarkException());
	}
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Mock
	Callable<String> mockCallable;

	@Mock
	Runnable mockRunnable;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Theory
	public void forCallable_invokingCallInvokesCallable() throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forCallable(recorder, mockCallable);
		callable.call();
		verify(mockCallable).call();
	}

	@Theory
	public void forCallable_invokingCallReturnsResultFromCallable(final String expectedResult) throws Exception {
		when(mockCallable.call()).thenReturn(expectedResult);
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forCallable(recorder, mockCallable);
		assertThat(callable.call()).isEqualTo(expectedResult);
	}
	
	@Theory(nullsAccepted = false)
	public void forCallable_throwingExceptionInCallableThrowsFromStatCallable(final Exception exp) throws Exception {
		when(mockCallable.call()).thenThrow(exp);
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forCallable(recorder, mockCallable);
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				callable.call();
			}
		}).isEqualTo(exp);
	}

	@Theory
	public void forRunnable_GivenResult_invokingCallInvokesCallable() throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable, null);
		callable.call();
		verify(mockRunnable).run();
	}

	@Theory
	public void forRunnable_GivenResult_invokingCallReturnsGivenResult(final String expectedResult) throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable, expectedResult);
		assertThat(callable.call()).isEqualTo(expectedResult);
	}

	@Theory(nullsAccepted = false)
	public void forRunnable_GivenResult_thrownExceptionReachesCaller(final String expectedResult, final RuntimeException exp) {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		doThrow(exp).when(mockRunnable).run();

		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable, expectedResult);
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				callable.call();
			}
		}).isEqualTo(exp);
	}

	@Theory
	public void forRunnable_NoResult_invokingCallInvokesRun() throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final StatsRecordingCallable<?> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable);
		callable.call();
		verify(mockRunnable).run();
	}

	@Theory(nullsAccepted = false)
	public void forRunnable_NoResult_thrownExceptionReachesCaller(final RuntimeException exp) {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		doThrow(exp).when(mockRunnable).run();

		final StatsRecordingCallable<?> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable);
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				callable.call();
			}
		}).isEqualTo(exp);
	}

	@Theory
	public void forCallable_NullRecorderFailsFast() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Recorder");
		StatsRecordingCallable.forCallable(null, mockCallable);
	}
	
	@Theory
	public void forCallable_NullCallableFailsFast() {
		expectedException.expect(IllegalArgumentException.class);
		StatsRecordingCallable.forCallable(StatisticsRecorder.newRecorder(), null);
	}

	@Theory
	public void forRunnable_withResult_NullRecorderFailsFast() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Recorder");
		StatsRecordingCallable.forRunnable(null, mockRunnable, null);
	}

	@Theory
	public void forRunnable_withResult_NullCallableFailsFast(final String result) {
		expectedException.expect(IllegalArgumentException.class);
		StatsRecordingCallable.forRunnable(StatisticsRecorder.newRecorder(), null, result);
	}

	@Theory
	public void forRunnable_noResult_NullRecorderFailsFast() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Recorder");
		StatsRecordingCallable.forRunnable(null, mockRunnable);
	}

	@Theory
	public void forRunnable_noResult_NullCallableFailsFast() {
		expectedException.expect(IllegalArgumentException.class);
		StatsRecordingCallable.forRunnable(StatisticsRecorder.newRecorder(), null);
	}

	@Theory
	public void forCallable_call_successIsRecordedWhenReturningNormally(final String result) throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();

		when(mockCallable.call()).thenReturn(result);

		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forCallable(recorder, mockCallable);

		callable.call();

		final Statistics snapshot = recorder.takeSnapshot();
		assertThat(snapshot.getSuccessCount()).isEqualTo(1);
		assertThat(snapshot.getErrorCount()).isEqualTo(0);
	}

	@Theory(nullsAccepted = false)
	public void forCallable_call_errorIsRecordedWhenExceptionOccurs(final Exception exception) throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();

		when(mockCallable.call()).thenThrow(exception);

		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forCallable(recorder, mockCallable);

		try {
			callable.call();
			fail("Expected exception not encountered");
		} catch (final Exception exp) {
			final Statistics snapshot = recorder.takeSnapshot();
			assertThat(snapshot.getSuccessCount()).isEqualTo(0);
			assertThat(snapshot.getErrorCount()).isEqualTo(1);
		}
	}

	@Theory
	public void forRunnable_withResult_call_successIsRecordedWhenReturningNormally(final String result) throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();


		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable, result);

		callable.call();

		final Statistics snapshot = recorder.takeSnapshot();
		assertThat(snapshot.getSuccessCount()).isEqualTo(1);
		assertThat(snapshot.getErrorCount()).isEqualTo(0);
	}

	@Theory(nullsAccepted = false)
	public void forRunnable_withResult_call_errorIsRecordedWhenExceptionOccurs(final RuntimeException exception,
																			   final String wantedReslt) {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();

		doThrow(exception).when(mockRunnable).run();

		final StatsRecordingCallable<String> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable, wantedReslt);

		try {
			callable.call();
			fail("Expected exception not encountered");
		} catch (final Exception exp) {
			final Statistics snapshot = recorder.takeSnapshot();
			assertThat(snapshot.getSuccessCount()).isEqualTo(0);
			assertThat(snapshot.getErrorCount()).isEqualTo(1);
		}
	}

	@Theory
	public void forRunnable_noResult_call_successIsRecordedWhenReturningNormally() throws Exception {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();

		final StatsRecordingCallable<?> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable);

		callable.call();

		final Statistics snapshot = recorder.takeSnapshot();
		assertThat(snapshot.getSuccessCount()).isEqualTo(1);
		assertThat(snapshot.getErrorCount()).isEqualTo(0);
	}

	@Theory(nullsAccepted = false)
	public void forRunnable_noResult_call_errorIsRecordedWhenExceptionOccurs(final RuntimeException exception) {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();

		doThrow(exception).when(mockRunnable).run();

		final StatsRecordingCallable<?> callable = StatsRecordingCallable.forRunnable(recorder, mockRunnable);

		try {
			callable.call();
			fail("Expected exception not encountered");
		} catch (final Exception exp) {
			final Statistics snapshot = recorder.takeSnapshot();
			assertThat(snapshot.getSuccessCount()).isEqualTo(0);
			assertThat(snapshot.getErrorCount()).isEqualTo(1);
		}
	}
}
