package com.paloski.statistics;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public final class ErrorStatisticsTest {

	public static final String DATA_POINTS__VALID_VALUES = "Data-Points::valid-values";
	public static final String DATA_POINTS__INVALID_VALUES = "Data-Points::invalid-values";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@DataPoints(DATA_POINTS__INVALID_VALUES)
	public static List<Long> getInvalidErrorCounts() {
		return Arrays.asList(-10L, -1L);
	}

	@DataPoints(DATA_POINTS__VALID_VALUES)
	public static List<Long> getErrorCounts() {
		return Arrays.asList(0L, 1L, 2L, 15L);
	}

	@DataPoints
	public static List<Map<Class<? extends Exception>, Long>> getExceptionMappings() {
		final Map<Class<? extends Exception>, Long> simple = new HashMap<>();
		simple.put(IOException.class, 1L);

		final Map<Class<? extends Exception>, Long> simpleInheritance = new HashMap<>();
		simpleInheritance.put(IOException.class, 100L);
		simpleInheritance.put(FileNotFoundException.class, 10L);
		simpleInheritance.put(Exception.class, 50L);

		final Map<Class<? extends Exception>, Long> runtimeCheckedMix = new HashMap<>();
		runtimeCheckedMix.put(RuntimeException.class, 100L);
		runtimeCheckedMix.put(Exception.class, 10L);

		return Arrays.asList(Collections.<Class<? extends Exception>, Long>emptyMap(),
							 simple,
							 simpleInheritance,
							 runtimeCheckedMix);
	}


	@Theory
	public void getErrorCount_returnsSameValueAsConstructorArgument(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts) {
		final ErrorStatistics statistics = ErrorStatistics.forFailures(errorCounts, Collections.<Class<? extends Exception>, Long>emptyMap());
		assertThat(statistics.getTotalErrorCount()).isEqualTo(errorCounts);
	}
	
	@Theory
	public void getErrorCount_returnsSameValueASConstructorArgument(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts,
																	final Map<Class<? extends Exception>, Long> attributedErrors) {
		final ErrorStatistics statistics = ErrorStatistics.forFailures(errorCounts, attributedErrors);
		
		long sum = errorCounts;
		for(final Long value : attributedErrors.values()) {
			sum += value;
		}
		assertThat(statistics.getTotalErrorCount()).isEqualTo(sum);
	}

	@Theory
	public void getExceptionTypeMap_returnsSameValueAsConstructorArgument(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts,
																		  final Map<Class<? extends Exception>, Long> attributedErrors) {
		final ErrorStatistics statistics = ErrorStatistics.forFailures(errorCounts, attributedErrors);
		assertThat(statistics.getCountOfExceptionTypes()).isEqualTo(attributedErrors);
	}

	@Theory
	public void getExceptionTypeMap_returnedMapIsUnmodifiable(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts,
																		  final Map<Class<? extends Exception>, Long> attributedErrors) {
		final ErrorStatistics statistics = ErrorStatistics.forFailures(errorCounts, attributedErrors);
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				statistics.getCountOfExceptionTypes().put(IllegalAccessException.class, 100L);
			}
		}).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				statistics.getCountOfExceptionTypes().remove(IOException.class);
			}
		}).isInstanceOf(UnsupportedOperationException.class);
	}

	@Theory
	public void getExceptionTypeMap_modifyingArgumentMapDoesNotAlterUnderlying(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts,
															  final Map<Class<? extends Exception>, Long> attributedErrors) {
		final Map<Class<? extends Exception>, Long> clone = new HashMap<>(attributedErrors);
		final ErrorStatistics statistics = ErrorStatistics.forFailures(errorCounts, clone);
		assertThat(statistics.getCountOfExceptionTypes()).isEqualTo(attributedErrors);
		clone.clear();
		assertThat(statistics.getCountOfExceptionTypes()).isEqualTo(attributedErrors);
	}

	@Theory
	public void mergeWith_mergingTwoStatsReturnsSumOfErrorAmounts(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long firstStatsErrorCounts,
																  final Map<Class<? extends Exception>, Long> firstMapping,
																  @FromDataPoints(DATA_POINTS__VALID_VALUES) final long secondStatsErrorCounts,
																  final Map<Class<? extends Exception>, Long> secondMapping) {
		assumeTrue(firstStatsErrorCounts + secondStatsErrorCounts >= 0L);
		final ErrorStatistics first = ErrorStatistics.forFailures(firstStatsErrorCounts, firstMapping);
		final ErrorStatistics second = ErrorStatistics.forFailures(secondStatsErrorCounts, secondMapping);

		final ErrorStatistics merged = first.mergeWith(second);
		assertThat(merged.getTotalErrorCount()).isEqualTo(first.getTotalErrorCount() + second.getTotalErrorCount());
	}

	@Theory
	public void mergeWith_mergingTwoStatsReturnsMapWithSameKeyValueTotals(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long firstStatsErrorCounts,
																		  final Map<Class<? extends Exception>, Long> firstMapping,
																		  @FromDataPoints(DATA_POINTS__VALID_VALUES) final long secondStatsErrorCounts,
																		  final Map<Class<? extends Exception>, Long> secondMapping) {
		assumeTrue(firstStatsErrorCounts + secondStatsErrorCounts >= 0L);
		final ErrorStatistics first = ErrorStatistics.forFailures(firstStatsErrorCounts, firstMapping);
		final ErrorStatistics second = ErrorStatistics.forFailures(secondStatsErrorCounts, secondMapping);

		final ErrorStatistics merged = first.mergeWith(second);
		for(final Class<? extends Exception> cls : firstMapping.keySet()) {
			final long firstValue = firstMapping.get(cls);
			final Long secondValue = secondMapping.get(cls);
			assertThat(merged.getCountOfExceptionTypes().get(cls)).isEqualTo(firstValue + (secondValue == null ? 0L : secondValue));
		}


		for(final Class<? extends Exception> cls : secondMapping.keySet()) {
			final Long firstValue = firstMapping.get(cls);
			final long secondValue = secondMapping.get(cls);
			assertThat(merged.getCountOfExceptionTypes().get(cls)).isEqualTo((firstValue == null ? 0L : firstValue)+ secondValue);
		}
	}

	@Theory
	public void mergeWith_reversingCallerDoesNotAlterResult(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long firstStatsErrorCounts,
																  final Map<Class<? extends Exception>, Long> firstMapping,
																  @FromDataPoints(DATA_POINTS__VALID_VALUES) final long secondStatsErrorCounts,
																  final Map<Class<? extends Exception>, Long> secondMapping) {
		assumeTrue(firstStatsErrorCounts + secondStatsErrorCounts >= 0L);
		final ErrorStatistics first = ErrorStatistics.forFailures(firstStatsErrorCounts, firstMapping);
		final ErrorStatistics second = ErrorStatistics.forFailures(secondStatsErrorCounts, secondMapping);

		assertThat(first.mergeWith(second)).isEqualTo(second.mergeWith(first));
	}

	@Theory
	public void forFailures_nullMapThrowsIllegalArgumentException(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long validErrCount) {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("null");
		ErrorStatistics.forFailures(validErrCount, null);
	}

	@Theory
	public void forUncategorizedFailures_negativeValueThrowsIllegalArgumentException(@FromDataPoints(DATA_POINTS__INVALID_VALUES) final long invalidErrorCounts) {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(Long.toString(invalidErrorCounts));
		ErrorStatistics.forUncategorizedFailureCount(invalidErrorCounts);
	}

	@Theory
	public void forFailures_negativeValueThrowsIllegalArgumentException(@FromDataPoints(DATA_POINTS__INVALID_VALUES) final long invalidErrorCounts,
																		final Map<Class<? extends Exception>, Long> classMapping) {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(Long.toString(invalidErrorCounts));
		ErrorStatistics.forFailures(invalidErrorCounts, classMapping);
	}

	@Theory
	public void objectIdentity_equalsUsesAllFields(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCount) {
		final ErrorStatistics errorStatsOne = ErrorStatistics.forUncategorizedFailureCount(errorCount);
		final ErrorStatistics errorStatsTwo = ErrorStatistics.forUncategorizedFailureCount(errorCount);

		assertThat(errorStatsOne).isEqualTo(errorStatsTwo);
		assertThat(errorStatsOne).isEqualToComparingFieldByField(errorStatsTwo);
	}

	@Theory
	public void objectIdentity_hashCodeIsEqualForEqualObjects(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCount) {
		final ErrorStatistics errorStatsOne = ErrorStatistics.forUncategorizedFailureCount(errorCount);
		final ErrorStatistics errorStatsTwo = ErrorStatistics.forUncategorizedFailureCount(errorCount);

		assertThat(errorStatsOne).isEqualTo(errorStatsTwo);
		assertThat(errorStatsOne.hashCode()).isEqualTo(errorStatsTwo.hashCode());
	}

	@Theory
	public void empty_emptyInstanceIsActuallyEmpty() {
		final ErrorStatistics empty = ErrorStatistics.empty();
		assertThat(empty.getTotalErrorCount()).isEqualTo(0);
	}

	@Theory
	public void toString_containsPertenentInformation(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCount,
													  final Map<Class<? extends Exception>, Long> mapping) {
		final ErrorStatistics stats = ErrorStatistics.forFailures(errorCount, mapping);
		assertThat(stats.toString()).contains(Long.toString(stats.getTotalErrorCount()))
									.contains(Long.toString(stats.getUncategorizedErrorCount()));
		for (final Class<? extends Exception> cls : mapping.keySet()) {
			assertThat(stats.toString()).contains(cls.getSimpleName());
		}
	}
}