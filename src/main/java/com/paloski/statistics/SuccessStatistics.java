package com.paloski.statistics;

import java.util.Objects;

/**
 * A class representing the statistics related to success recorded by a {@link StatisticsRecorder}.
 * <p/>
 * This class is immutable and (therefore) thread safe.
 *
 * @author Adam
 */
public final class SuccessStatistics {

	private static final SuccessStatistics EMPTY = new SuccessStatistics(0L);

	private final long mSuccessCount;

	/**
	 * Creates a new SuccessStatistics object that stored the number of successes that is passed
	 * into it
	 *
	 * @param successCount
	 * 		The number of successes encountered. Must not be less than 0L.
	 */
	/* package */
	private SuccessStatistics(final long successCount) {
		if (successCount < 0L) {
			throw new IllegalArgumentException("Success Count (" + successCount + ") cannot be negative");
		}
		mSuccessCount = successCount;
	}

	/**
	 * A factory method that returns a SuccessStatistics object based upon the failure count
	 * presented.
	 *
	 * @param successCount
	 * 		the number of successes encountered.
	 *
	 * @return A non-null SuccessStatistics object containing the given success count.
	 */
	/* package */ static SuccessStatistics forSuccessCount(final long successCount) {
		return new SuccessStatistics(successCount);
	}

	/**
	 * Obtains an empty SuccessStatistics object that contains no statistical information.
	 *
	 * @return A non-null, empty SuccessStatistics object.
	 */
	/* package */ static SuccessStatistics empty() {
		return EMPTY;
	}

	/**
	 * Merges this SuccessStatistics with another, creating a new SuccessStatistics object that
	 * contains the sum of the stats stored by this object and the other.
	 * <p/>
	 * Numeric overflow is not protected against in this operation.
	 *
	 * @param other
	 * 		Another success statistics object that this one should be merged with.
	 *
	 * @return A new SuccessStatistics object that contains the sum of the stats of both this and
	 * {@code other}
	 */
	public SuccessStatistics mergeWith(final SuccessStatistics other) {
		return forSuccessCount(mSuccessCount + other.getSuccessCount());
	}

	/**
	 * Obtains the number of successes recorded in this Statistics object
	 *
	 * @return The number of successes that this statistics object records.
	 */
	public long getSuccessCount() {
		return mSuccessCount;
	}


	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof SuccessStatistics)) return false;
		final SuccessStatistics that = (SuccessStatistics) o;
		return getSuccessCount() == that.getSuccessCount();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSuccessCount());
	}
}
