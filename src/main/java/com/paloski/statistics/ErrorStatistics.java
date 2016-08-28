package com.paloski.statistics;

import com.paloski.annotation.ProxyInterface;

import java.util.Objects;

/**
 * A class representing the statistics related to errors recorded by a {@link StatisticsRecorder}.
 * <p/>
 * This class is immutable and (therefore) thread safe.
 *
 * @author Adam
 */
public final class ErrorStatistics {

	private static final ErrorStatistics EMPTY = new ErrorStatistics(0);

	private final long mErrorCount;

	/**
	 * A factory method that returns a ErrorStatistics object based upon the failure count
	 * presented.
	 *
	 * @param failureCount
	 * 		the number of failures encountered.
	 *
	 * @return A non-null ErrorStatistics object containing the given failure count.
	 */
	/* package */ static ErrorStatistics forFailureCount(final long failureCount) {
		if(failureCount == 0L) {
			return empty();
		} else {
			return new ErrorStatistics(failureCount);
		}
	}

	/**
	 * Creates a new ErrorStatistics that takes in a number of failures as its seed data.
	 *
	 * @param failureCount The number of failures recorded in this statistics object.
	 */
	private ErrorStatistics(final long failureCount) {
		if (failureCount < 0L) {
			throw new IllegalArgumentException("A negative number of errors (" + failureCount + ")is not handled by this function");
		}
		mErrorCount = failureCount;
	}

	/**
	 * Obtains an empty ErrorStatistics object that contains no statistical information.
	 *
	 * @return A non-null, empty ErrorStatistics object.
	 */
	/* package */ static ErrorStatistics empty() {
		return EMPTY;
	}

	/**
	 * Merges this ErrorStatistics object with another, creating a ErrorStatistics that is the sum
	 * of the statistics of {@code this} object and the other object.
	 * <p/>
	 * Numeric overflow is not handled by this function.
	 *
	 * @param other
	 * 		Another ErrorStatistics object that is to be merged with this one.
	 *
	 * @return A new ErrorStatistics object containing the sum of this statistics and the other.
	 */
	public ErrorStatistics mergeWith(final ErrorStatistics other) {
		return new ErrorStatistics(mErrorCount + other.getErrorCount());
	}

	/**
	 * Obtains the number of errors recorded in this ErrorStatistics object.
	 *
	 * @return The number of errors reported by this object.
	 */
	public long getErrorCount() {
		return mErrorCount;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof ErrorStatistics)) return false;
		final ErrorStatistics that = (ErrorStatistics) o;
		return getErrorCount() == that.getErrorCount();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getErrorCount());
	}

}
