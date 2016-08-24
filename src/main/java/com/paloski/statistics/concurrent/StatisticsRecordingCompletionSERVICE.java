package com.paloski.statistics.concurrent;

import com.paloski.statistics.Statistics;
import com.paloski.statistics.StatisticsRecorder;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A CompletionService composed of another CompletionService and
 *
 * @author Adam
 */
public final class StatisticsRecordingCompletionService<V> implements CompletionService<V>  {

	private final StatisticsRecorder mRecorder = StatisticsRecorder.newRecorder();
	private final CompletionService<V> mDelegate;

	/**
	 * Creates a new StatisticsRecordingCompletionService which delegates to the given service and
	 * records the result of each task submitted to it.
	 *
	 * @param delegate A CompletionService to be delegated to for all CompletionService method calls.
	 */
	public StatisticsRecordingCompletionService(final CompletionService<V> delegate) {
		if(delegate == null) {
			throw new NullPointerException("Delegate cannot be null");
		}
		mDelegate = delegate;
	}

	@Override
	public Future<V> submit(final Callable<V> task) {
		return mDelegate.submit(new StatRecordingCallable(task));
	}

	@Override
	public Future<V> submit(final Runnable task, final V result) {
		return mDelegate.submit(new StatRecordingCallable(Executors.callable(task, result)));
	}

	@Override
	public Future<V> take() throws InterruptedException {
		return mDelegate.take();
	}

	@Override
	public Future<V> poll() {
		return mDelegate.poll();
	}

	@Override
	public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		return mDelegate.poll(timeout, unit);
	}

	/**
	 * Obtains the Statistics on submitted callables that have been submitted to this
	 * CompletionService.
	 *
	 * @return A non-null Statistics object containing statistics about what has been submitted to
	 * this service.
	 */
	public Statistics getStatistics() {
		return mRecorder.takeSnapshot();
	}

	private class StatRecordingCallable implements Callable<V> {

		private final Callable<V> mDelegate;

		private StatRecordingCallable(final Callable<V> delegate) {
			if(delegate == null) {
				throw new NullPointerException("Cannot handle a null callable delegate");
			}
			mDelegate = delegate;
		}

		@Override
		public V call() throws Exception {
			try {
				final V result = mDelegate.call();
				mRecorder.recordSuccess();
				return result;
			} catch (final Exception exp) {
				mRecorder.recordError(exp);
				throw exp;
			}
		}
	}
}
