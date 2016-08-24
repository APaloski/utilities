package com.paloski.statistics;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public final class SuccessStatisticsTest {

	public static final String DATA_POINTS__VALID_VALUES = "Data-Points::valid-values";
	public static final String DATA_POINTS__INVALID_VALUES = "Data-Points::invalid-values";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@DataPoints(DATA_POINTS__INVALID_VALUES)
	public static List<Long> getInvalidSuccessCounts() {
		return Arrays.asList(-10L, -1L, Long.MIN_VALUE);
	}

	@DataPoints(DATA_POINTS__VALID_VALUES)
	public static List<Long> getSuccessCounts() {
		return Arrays.asList(0L, 1L, 2L, 15L, Long.MAX_VALUE);
	}

	@Theory
	public void getSuccessCount_returnsSameValueAsConstructorArgument(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long successCount) {
		final SuccessStatistics statistics = SuccessStatistics.forSuccessCount(successCount);
		assertThat(statistics.getSuccessCount()).isEqualTo(successCount);
	}

	@Theory
	public void mergeWith_mergingTwoStatsReturnsSumOfSuccessAmounts(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long firstStatsSuccessCount,
																	@FromDataPoints(DATA_POINTS__VALID_VALUES) final long secondStatsSuccessCount) {
		assumeTrue(firstStatsSuccessCount + secondStatsSuccessCount >= 0L);
		final SuccessStatistics first = SuccessStatistics.forSuccessCount(firstStatsSuccessCount);
		final SuccessStatistics second = SuccessStatistics.forSuccessCount(secondStatsSuccessCount);

		final SuccessStatistics merged = first.mergeWith(second);
		assertThat(merged.getSuccessCount()).isEqualTo(firstStatsSuccessCount + secondStatsSuccessCount);
	}

	@Theory
	public void constructor_negativeValueThrowsIllegalArgumentException(@FromDataPoints(DATA_POINTS__INVALID_VALUES) final long invalidSuccessCount) {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(Long.toString(invalidSuccessCount));
		SuccessStatistics.forSuccessCount(invalidSuccessCount);
	}

	@Theory
	public void objectIdentity_allFieldsAreUsedInEquals(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long validSuccessCount) {
		final SuccessStatistics successStatistics1 = SuccessStatistics.forSuccessCount(validSuccessCount);
		final SuccessStatistics successStatistics2 = SuccessStatistics.forSuccessCount(validSuccessCount);

		assertThat(successStatistics1).isEqualTo(successStatistics2);
		assertThat(successStatistics1).isEqualToComparingFieldByField(successStatistics2);
	}

	@Theory
	public void objectIdentity_hashIsEqualForEqualObjects(@FromDataPoints(DATA_POINTS__VALID_VALUES) final long validSuccessCount) {
		final SuccessStatistics successStatistics1 = SuccessStatistics.forSuccessCount(validSuccessCount);
		final SuccessStatistics successStatistics2 = SuccessStatistics.forSuccessCount(validSuccessCount);

		assertThat(successStatistics1).isEqualTo(successStatistics2);
		assertThat(successStatistics1.hashCode()).isEqualTo(successStatistics2.hashCode());
	}

	@Theory
	public void empty_returnsEmptySuccess() {
		SuccessStatistics stats = SuccessStatistics.empty();
		assertThat(stats.getSuccessCount()).isEqualTo(0L);
	}

}