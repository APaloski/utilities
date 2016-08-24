package com.paloski.statistics;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.FormattableFlags;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public final class ErrorStatisticsTest {

	public static final String DATA_POINTS__VALID_VALUES = "Data-Points::valid-values";
	public static final String DATA_POINTS__INVALID_VALUES = "Data-Points::invalid-values";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@DataPoints(DATA_POINTS__INVALID_VALUES)
	public static List<Long> getInvalidErrorCounts() {
		return Arrays.asList(-10L, -1L, Long.MIN_VALUE);
	}

	@DataPoints(DATA_POINTS__VALID_VALUES)
	public static List<Long> getErrorCounts() {
		return Arrays.asList(0L, 1L, 2L, 15L, Long.MAX_VALUE);
	}


	@Theory
	public void getErrorCount_returnsSameValueAsConstructorArgument(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCounts) {
		final ErrorStatistics statistics = ErrorStatistics.forFailureCount(errorCounts);
		assertThat(statistics.getErrorCount()).isEqualTo(errorCounts);
	}

	@Theory
	public void mergeWith_mergingTwoStatsReturnsSumOfSuccessAmounts(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long firstStatsErrorCounts,
																	@FromDataPoints(DATA_POINTS__VALID_VALUES) final long secondStatsErrorCounts) {
		assumeTrue(firstStatsErrorCounts + secondStatsErrorCounts >= 0L);
		final ErrorStatistics first = ErrorStatistics.forFailureCount(firstStatsErrorCounts);
		final ErrorStatistics second = ErrorStatistics.forFailureCount(secondStatsErrorCounts);

		final ErrorStatistics merged = first.mergeWith(second);
		assertThat(merged.getErrorCount()).isEqualTo(firstStatsErrorCounts + secondStatsErrorCounts);
	}

	@Theory
	public void constructor_negativeValueThrowsIllegalArgumentException(@FromDataPoints(DATA_POINTS__INVALID_VALUES) final long invalidErrorCounts) {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(Long.toString(invalidErrorCounts));
		ErrorStatistics.forFailureCount(invalidErrorCounts);
	}

	@Theory
	public void objectIdentity_equalsUsesAllFields(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCount) {
		final ErrorStatistics errorStatsOne = ErrorStatistics.forFailureCount(errorCount);
		final ErrorStatistics errorStatsTwo = ErrorStatistics.forFailureCount(errorCount);

		assertThat(errorStatsOne).isEqualTo(errorStatsTwo);
		assertThat(errorStatsOne).isEqualToComparingFieldByField(errorStatsTwo);
	}

	@Theory
	public void objectIdentity_hashCodeIsEqualForEqualObjects(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long errorCount) {
		final ErrorStatistics errorStatsOne = ErrorStatistics.forFailureCount(errorCount);
		final ErrorStatistics errorStatsTwo = ErrorStatistics.forFailureCount(errorCount);

		assertThat(errorStatsOne).isEqualTo(errorStatsTwo);
		assertThat(errorStatsOne.hashCode()).isEqualTo(errorStatsTwo.hashCode());
	}

	@Theory
	public void empty_emptyInstanceIsActuallyEmpty() {
		final ErrorStatistics empty = ErrorStatistics.empty();
		assertThat(empty.getErrorCount()).isEqualTo(0);
	}
}