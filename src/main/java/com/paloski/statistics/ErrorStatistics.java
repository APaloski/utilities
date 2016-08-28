package com.paloski.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class representing the statistics related to errors recorded by a {@link StatisticsRecorder}.
 * <p/>
 * This class is immutable and (therefore) thread safe.
 *
 * @author Adam
 */
public final class ErrorStatistics {

	private static final ErrorStatistics EMPTY = new ErrorStatistics(0, Collections.<Class<? extends Exception>, Long>emptyMap());

	private final long mErrorCount;
	private final /*Immutable*/ Map<Class<? extends Exception>, Long> mExceptionTypeMap;

	/**
	 * A factory method that returns a ErrorStatistics object based upon the failure count
	 * presented.
	 *
	 * @param failureCount
	 * 		the number of failures encountered.
	 *
	 * @return A non-null ErrorStatistics object containing the given failure count.
	 */
	/* package */
	static ErrorStatistics forUncategorizedFailureCount(final long failureCount) {
		if (failureCount == 0L) {
			return empty();
		} else {
			return new ErrorStatistics(failureCount, Collections.<Class<? extends Exception>, Long>emptyMap());
		}
	}

	/**
	 * A factory method that returns an ErrorStatistics object based upon an unattributed failure
	 * count as well as a number of attributed failures.
	 *
	 * @param unattributedFailures
	 * 		The number of unattributed failures
	 * @param typedErrors
	 * 		A map from known exception types to the number of errors that occurred from them.
	 *
	 * @return A non-null ErrorStatistics containing the given failures
	 */
	/* package */
	static ErrorStatistics forFailures(final long unattributedFailures,
									   final Map<Class<? extends Exception>, Long> typedErrors) {
		if (null == typedErrors) {
			throw new IllegalArgumentException("Cannot handle null map of exceptions, use empty instead");
		} else if (typedErrors.isEmpty()) {
			return forUncategorizedFailureCount(unattributedFailures);
		} else {
			return new ErrorStatistics(unattributedFailures, typedErrors);
		}
	}

	/**
	 * Creates a new ErrorStatistics that takes in a number of failures as its seed data.
	 *
	 * @param failureCount
	 * 		The number of failures recorded in this statistics object.
	 * @param exceptionTypeMap
	 * 		A mapping from each exception to how often they occurred.
	 */
	private ErrorStatistics(final long failureCount,
							final Map<Class<? extends Exception>, Long> exceptionTypeMap) {
		if (failureCount < 0L) {
			throw new IllegalArgumentException("A negative number of errors (" + failureCount + ")is not handled by this function");
		}

		mErrorCount = failureCount;
		mExceptionTypeMap = Collections.unmodifiableMap(new HashMap<>(exceptionTypeMap));
	}

	/**
	 * Obtains an empty ErrorStatistics object that contains no statistical information.
	 *
	 * @return A non-null, empty ErrorStatistics object.
	 */
	/* package */
	static ErrorStatistics empty() {
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
		final Map<Class<? extends Exception>, Long> myExceptionTypeMap = getCountOfExceptionTypes();
		final Map<Class<? extends Exception>, Long> otherExceptionTypeMap = other.getCountOfExceptionTypes();

		final Map<Class<? extends Exception>, Long> mergedMap = new HashMap<>(myExceptionTypeMap.size() + otherExceptionTypeMap.size());
		for (final Class<? extends Exception> cls : myExceptionTypeMap.keySet()) {
			final Long otherCount = otherExceptionTypeMap.get(cls);
			mergedMap.put(cls, myExceptionTypeMap.get(cls) + (null == otherCount ? 0L : otherCount));
		}

		//Now do it for the other side...
		for (final Class<? extends Exception> cls : otherExceptionTypeMap.keySet()) {
			if (!mergedMap.containsKey(cls)) {
				mergedMap.put(cls, otherExceptionTypeMap.get(cls));
			}
		}

		return new ErrorStatistics(getUncategorizedErrorCount() + other.getUncategorizedErrorCount(),
								   mergedMap);
	}

	/**
	 * Obtains the uncategorized error count of this. This is all errors that were not associated
	 * to
	 * an Exception type.
	 *
	 * @return The count of errors not associated to an Exception type.
	 */
	public long getUncategorizedErrorCount() {
		return mErrorCount;
	}

	/**
	 * Obtains the number of errors recorded in this ErrorStatistics object.
	 *
	 * @return The number of errors reported by this object.
	 */
	public long getTotalErrorCount() {
		Long total = mErrorCount;
		for (final Long value : mExceptionTypeMap.values()) {
			total += value;
		}
		return total;
	}

	/**
	 * Returns an unmodifiable map with the count of how many times each exception type was
	 * recorded
	 * to occur.
	 *
	 * @return A non-null, but unmodifiable map of exception class -> count of occurrences
	 */
	public Map<Class<? extends Exception>, Long> getCountOfExceptionTypes() {
		return mExceptionTypeMap;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof ErrorStatistics)) return false;
		final ErrorStatistics that = (ErrorStatistics) o;
		return getTotalErrorCount() == that.getTotalErrorCount() &&
			   Objects.equals(mExceptionTypeMap, that.mExceptionTypeMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTotalErrorCount(), mExceptionTypeMap);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Error Statistics\n")
			   .append("================\n");
		for (final Map.Entry<Class<? extends Exception>, Long> occurrenceEntry : mExceptionTypeMap.entrySet()) {
			builder.append(occurrenceEntry.getKey().getCanonicalName()).append(": ").append(occurrenceEntry.getValue())
				   	.append(" (").append(String.format("%.2f%%", (((float) occurrenceEntry.getValue()) / getTotalErrorCount()) * 100)).append( ")\n");
		}
		builder.append("Uncategorized: ").append(getUncategorizedErrorCount()).append(" (").append(String.format("%.2f%%", (((float) getUncategorizedErrorCount()) / getTotalErrorCount()) * 100)).append(")\n")
				.append("--------\n")
				.append("Total: ").append(getTotalErrorCount());
		return builder.toString();
	}
}
