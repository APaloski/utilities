package com.paloski.statistics.concurrent;

import com.paloski.statistics.StatisticsRecorder;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * A simple Callable wrapper that records statistics about the results of the underlying callable
 * invocation into a given StatisticsRecorder object
 *
 * @author Adam
 */
public final class StatsRecordingCallable<V> implements Callable<V> {

	private final StatisticsRecorder mRecorder;
	private final Callable<V> mTarget;

	/**
	 * Creates a new StatsRecordingCallable that will invoke the given Callable when it is called,
	 * recording a successful run or any errors that occur when the call method of that callable
	 * completes.
	 *
	 * @param recorder
	 * 		A StatisticsRecorder object to be informed of the result of invoking {@link #call()}
	 * @param callable
	 * 		The underlying callable to be invoked when {@link #call()} is invoked
	 * @param <V>
	 * 		The type of the result of invoking {@code call}
	 *
	 * @return A new, non-null StatsRecordingCallable that will invoke callable and record the
	 * results in recorder.
	 */
	public static <V> StatsRecordingCallable<V> forCallable(final StatisticsRecorder recorder,
															final Callable<V> callable) {
		return new StatsRecordingCallable<>(recorder, callable);
	}

	/**
	 * Creates a new StatsRecordingCallable that will invoke the given Runnable when it is called,
	 * recording a successful run or any errors that occur when the call method of that callable
	 * completes. It will then return {@code result}.
	 *
	 * @param recorder
	 * 		A StatisticsRecorder object to be informed of the result of invoking {@link #call()}
	 * @param task
	 * 		The underlying Runnable to be invoked when {@link #call()} is invoked
	 * @param result
	 * 		The result of invoking {@code call} to be returned when {@code task.run()} completes
	 * 		successfully.
	 * @param <V>
	 * 		The type of the result of invoking {@code call}
	 *
	 * @return A new, non-null StatsRecordingCallable that will invoke runnable and record the
	 * results in recorder.
	 */
	public static <V> StatsRecordingCallable<V> forRunnable(final StatisticsRecorder recorder,
															final Runnable task,
															final V result) {
		if (task == null) {
			throw new IllegalArgumentException("Runnable task cannot be null");
		}
		return forCallable(recorder, Executors.callable(task, result));
	}

	/**
	 * Creates a new StatsRecordingCallable that will invoke the given Runnable when it is called,
	 * recording a successful run or any errors that occur when the call method of that callable
	 * completes. The result from invoking call on the returned object is undefined.
	 *
	 * @param recorder
	 * 		A StatisticsRecorder object to be informed of the result of invoking {@link #call()}
	 * @param task
	 * 		The underlying Runnable to be invoked when {@link #call()} is invoked
	 *
	 * @return A new, non-null StatsRecordingCallable that will invoke runnable and record the
	 * results in recorder.
	 */
	public static StatsRecordingCallable<?> forRunnable(final StatisticsRecorder recorder,
														final Runnable task) {
		if (task == null) {
			throw new IllegalArgumentException("Runnable task cannot be null");
		}
		return forCallable(recorder, Executors.callable(task));
	}

	private StatsRecordingCallable(final StatisticsRecorder recorder, final Callable<V> target) {
		if (recorder == null) {
			throw new IllegalArgumentException("Recorder cannot be null");
		} else if (target == null) {
			throw new IllegalArgumentException("Callable target cannot be null");
		}
		mRecorder = recorder;
		mTarget = target;
	}

	@Override
	public V call() throws Exception {
		try {
			final V result = mTarget.call();
			mRecorder.recordSuccess();
			return result;
		} catch (final Exception exp) {
			mRecorder.recordError(exp);
			throw exp;
		}
	}

}
