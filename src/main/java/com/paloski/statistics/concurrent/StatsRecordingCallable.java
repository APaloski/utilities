package com.paloski.statistics.concurrent;

import com.paloski.statistics.Statistics;
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

	public static <V> StatsRecordingCallable<V> forCallable(final StatisticsRecorder recorder,
															final Callable<V> callable) {
		return new StatsRecordingCallable<>(recorder, callable);
	}

	public static <V> StatsRecordingCallable<V> forRunnable(final StatisticsRecorder recorder,
															final Runnable task,
															final V result) {
		if(task == null) {
			throw new IllegalArgumentException("Runnable task cannot be null");
		}
		return forCallable(recorder, Executors.callable(task, result));
	}

	public static StatsRecordingCallable<?> forRunnable(final StatisticsRecorder recorder,
														final Runnable task) {
		if(task == null) {
			throw new IllegalArgumentException("Runnable task cannot be null");
		}
		return forCallable(recorder, Executors.callable(task));
	}

	private StatsRecordingCallable(final StatisticsRecorder recorder, final Callable<V> target) {
		if(recorder == null) {
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
