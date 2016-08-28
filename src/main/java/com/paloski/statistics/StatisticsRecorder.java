package com.paloski.statistics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread safe class that allows for recording of statistics about a running process.
 * <p/>
 * Successes of a procedural run can be recorded by invoking the {@link #recordSuccess()},
 * conversely {@link #recordError(Exception)} can be used to record an error in processing of a
 * procedural run.
 */
public final class StatisticsRecorder {

	private final ReadWriteLock mLock = new ReentrantReadWriteLock();

	private final AtomicLong mSuccessCount;
	private final AtomicLong mFailureCount;

	private StatisticsRecorder(final long startingSuccess,
							   final long startingFailure) {
		mSuccessCount = new AtomicLong(startingSuccess);
		mFailureCount = new AtomicLong(startingFailure);
	}

	/**
	 * Creates a new, empty StatisticsRecorder that is initialized with empty error and success
	 * statistics.
	 *
	 * @return A new, empty StatisticsRecorder
	 */
	public static StatisticsRecorder newRecorder() {
		return new StatisticsRecorder(0L, 0L);
	}

	/**
	 * Creates a new StatisticsRecorder that is seeded by the given Statistics object, starting
	 * recording as if it had recorded the information of the seed.
	 * <p/>
	 * Invoking {@link #takeSnapshot()} on the given object without invoking {@link
	 * #recordSuccess()} or
	 * {@link #recordError(Exception)} will return a Statistics object that is equivalent to the
	 * seed object.
	 *
	 * @param seed
	 * 		The non-null seed Statistics object that will drive the starting point of the new
	 * 		recorder.
	 *
	 * @return A new StatisticsRecorder that starts with a state matching a recorder that produced
	 * the seed object.
	 */
	public static StatisticsRecorder newSeededRecorder(final Statistics seed) {
		return new StatisticsRecorder(seed.getSuccessCount(), seed.getErrorCount());
	}

	/**
	 * Records that the process ran successfully and succeed.
	 */
	public void recordSuccess() {
		final Lock writeLock = mLock.writeLock();
		writeLock.lock();
		try {
			mSuccessCount.incrementAndGet();
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Records that an error occurred during the process this recorder is recording statistics
	 * about.
	 *
	 * @param exp
	 * 		The exception that occurred. Currently unused.
	 */
	public void recordError(final Exception exp) {
		final Lock writeLock = mLock.writeLock();
		writeLock.lock();
		try {
			mFailureCount.incrementAndGet();
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Takes a snapshot of the Statistics being recorded by this object at the current point, saving
	 * it into an immutable Statistics object.
	 *
	 * @return A non-null Statistics object representing the current state of this recorder.
	 */
	public Statistics takeSnapshot() {
		final Lock readLock = mLock.readLock();
		readLock.lock();
		try {
			return new Statistics(SuccessStatistics.forSuccessCount(mSuccessCount.get()),
								  ErrorStatistics.forFailureCount(mFailureCount.get()));
		} finally {
			readLock.unlock();
		}
	}

}
