package com.paloski.statistics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that records stats about a multi-item process, recording the success and failure rate of
 * the process and allowing snapshots of the stats to be taken at any point.
 */
public final class StatisticsRecorder {

	private final AtomicLong mSuccessCount;
	private final AtomicLong mFailureCount;

	private StatisticsRecorder(final long startingSuccess,
							   final long startingFailure) {
		mSuccessCount = new AtomicLong(startingSuccess);
		mFailureCount = new AtomicLong(startingFailure);
	}

	public static StatisticsRecorder newRecorder() {
		return new StatisticsRecorder(0L, 0L);
	}

	public static StatisticsRecorder newSeededRecorder(final Statistics seed) {
		return new StatisticsRecorder(seed.getSuccessCount(), seed.getErrorCount());
	}

	public void recordSuccess() {
		mSuccessCount.incrementAndGet();
	}

	public void recordError(final Exception exp) {
		mFailureCount.incrementAndGet();
	}

	public Statistics takeSnapshot() {
		return new Statistics(SuccessStatistics.forSuccessCount(mSuccessCount.get()),
							  ErrorStatistics.forFailureCount(mFailureCount.get()));
	}

}
