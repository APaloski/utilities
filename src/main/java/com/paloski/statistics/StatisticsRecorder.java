package com.paloski.statistics;

/**
 * A class that records stats about a multi-item process, recording the success and failure rate of
 * the process and allowing snapshots of the stats to be taken at any point.
 */
public final class StatisticsRecorder {

	private long mSuccessCount;
	private long mFailureCount;

	private StatisticsRecorder(final long startingSuccess,
							   final long startingFailure) {
		mSuccessCount = startingSuccess;
		mFailureCount = startingFailure;
	}

	public static StatisticsRecorder newRecorder() {
		return new StatisticsRecorder(0L, 0L);
	}

	public static StatisticsRecorder newSeededRecorder(final Statistics seed) {
		return new StatisticsRecorder(seed.getSuccessCount(), seed.getErrorCount());
	}

	public void recordSuccess() {
		mSuccessCount++;
	}

	public void recordError(final Exception exp) {
		mFailureCount++;
	}

	public Statistics takeSnapshot() {
		return new Statistics(SuccessStatistics.forSuccessCount(mSuccessCount),
							  ErrorStatistics.forFailureCount(mFailureCount));
	}

}
