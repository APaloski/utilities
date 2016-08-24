package com.paloski.statistics;

import java.util.Objects;

/**
 * A set of statistics comprised of the error and success statistics recorded by a {@link
 * StatisticsRecorder}.
 * <p/>
 * This class is Immutable, and (therefore) thread safe.
 *
 * @author Adam
 */
public final class Statistics {

	private final ErrorStatistics mErrorStatistics;
	private final SuccessStatistics mSuccessStatistics;

	/**
	 * Creates a new Statistics object from a non-null SuccessStatistics object and a non-null
	 * ErrorStatistics object.
	 *
	 * @param successStatistics
	 * 		The statistics of success that make up the success information of these overall statistics
	 * @param errorStatistics
	 * 		The statistics of errors that make up the success information of these overall statistics
	 */
	/* package */ Statistics(final SuccessStatistics successStatistics, final ErrorStatistics errorStatistics) {
		if (successStatistics == null) {
			throw new NullPointerException("Null success stats parameter is not allowed");
		} else if (errorStatistics == null) {
			throw new NullPointerException("Null error stats parameter is not allowed");
		}
		mErrorStatistics = errorStatistics;
		mSuccessStatistics = successStatistics;
	}

	/**
	 * Merges this statistics object with another Statistics object, creating a new Statistics
	 * object that is the sum of the two given statistics objects.
	 *
	 * @param other
	 * 		Another non-null Statistics object to be merged with this one.
	 *
	 * @return A new Statistics object that is the sum of the statistical data of both this and the
	 * other object.
	 */
	public Statistics mergeWith(final Statistics other) {
		return new Statistics(getSuccessStatistics().mergeWith(other.getSuccessStatistics()),
							  getErrorStatistics().mergeWith(other.getErrorStatistics()));
	}

	/**
	 * Obtains the total number of events (successes and errors) encountered by this Statistics
	 * object.
	 *
	 * @return The sum of {@link #getSuccessCount()} and {@link #getErrorCount()}
	 */
	public long getEventCount() {
		return getSuccessCount() + getErrorCount();
	}

	/**
	 * Obtains the number of successes recorded in this Statistics object.
	 *
	 * @return The number of successes
	 */
	public long getSuccessCount() {
		return mSuccessStatistics.getSuccessCount();
	}

	/**
	 * Obtains the number of errors recorded in this Statistics object.
	 *
	 * @return The number of errors
	 */
	public long getErrorCount() {
		return mErrorStatistics.getErrorCount();
	}

	/**
	 * Obtains more specific statistics about the errors that were recorded as a part of these
	 * statistics
	 *
	 * @return A non-null ErrorStatistics object that contains more detailed information about errors recorded.
	 */
	public ErrorStatistics getErrorStatistics() {
		return mErrorStatistics;
	}

	/**
	 * Obtains more specific statistics about the successes that were recorded as a part of these
	 * statistics.
	 *
	 * @return A non-null SuccessStatistics that contains more specific information about successes
	 * recorded.
	 */
	public SuccessStatistics getSuccessStatistics() {
		return mSuccessStatistics;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Statistics)) return false;
		final Statistics that = (Statistics) o;
		return Objects.equals(getErrorStatistics(), that.getErrorStatistics()) &&
			   Objects.equals(getSuccessStatistics(), that.getSuccessStatistics());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getErrorStatistics(), getSuccessStatistics());
	}
}
