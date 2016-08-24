package com.paloski.statistics;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public final class StatisticsTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@DataPoints
	public static List<SuccessStatistics> getSuccessStats() {
		return Arrays.asList(SuccessStatistics.forSuccessCount(10),
							 SuccessStatistics.forSuccessCount(15),
							 SuccessStatistics.forSuccessCount(32),
							 SuccessStatistics.forSuccessCount(54),
							 SuccessStatistics.forSuccessCount(0));
	}

	@DataPoints
	public static List<ErrorStatistics> getErrorStats() {
		return Arrays.asList(ErrorStatistics.forFailureCount(11),
							 ErrorStatistics.forFailureCount(0),
							 ErrorStatistics.forFailureCount(15),
							 ErrorStatistics.forFailureCount(32),
							 ErrorStatistics.forFailureCount(87));
	}

	@Theory
	public void getEventCount_returnsSumOfSuccessAndError(final SuccessStatistics successStatistics,
														  final ErrorStatistics errorStatistics) {
		final Statistics sut = new Statistics(successStatistics, errorStatistics);
		assertThat(sut.getEventCount()).isEqualTo(successStatistics.getSuccessCount() + errorStatistics.getErrorCount());
	}

	@Theory
	public void getSuccessCount_returnsValueEqualToSuccessStats(final SuccessStatistics successStatistics,
																final ErrorStatistics errorStatistics) {
		final Statistics sut = new Statistics(successStatistics, errorStatistics);
		assertThat(sut.getSuccessCount()).isEqualTo(successStatistics.getSuccessCount());
	}

	@Theory
	public void getErrorCount_returnedValueEqualToErrorStatus(final SuccessStatistics successStatistics,
															  final ErrorStatistics errorStatistics) {
		final Statistics sut = new Statistics(successStatistics, errorStatistics);
		assertThat(sut.getErrorCount()).isEqualTo(errorStatistics.getErrorCount());
	}

	@Theory
	public void constructor_rejectsNullSuccessStats(final ErrorStatistics errorStatistics) {
		expected.expect(NullPointerException.class);
		new Statistics(null, errorStatistics);
	}

	@Theory
	public void constructor_rejectsNullErrorStats(final SuccessStatistics successStatistics) {
		expected.expect(NullPointerException.class);
		new Statistics(successStatistics, null);
	}

	@Theory
	public void getSuccessStatistics_returnsEquivalentToConstructorArg(final SuccessStatistics successStatistics,
																	   final ErrorStatistics errorStatistics) {
		final Statistics sut = new Statistics(successStatistics, errorStatistics);
		assertThat(sut.getSuccessStatistics()).isEqualTo(successStatistics);
	}

	@Theory
	public void getErrorStatistics_returnsEquivalentToConstructorArg(final SuccessStatistics successStatistics,
																	 final ErrorStatistics errorStatistics) {
		final Statistics sut = new Statistics(successStatistics, errorStatistics);
		assertThat(sut.getErrorStatistics()).isEqualTo(errorStatistics);
	}

	@Theory
	public void mergeWith_errorAndSuccessIsEquivalentToSeparateMerging(final SuccessStatistics successStatisticsOne,
																	   final SuccessStatistics successStatisticsTwo,
																	   final ErrorStatistics errorStatisticsOne,
																	   final ErrorStatistics errorStatisticsTwo) {
		final Statistics statsOne = new Statistics(successStatisticsOne, errorStatisticsOne);
		final Statistics statsTwo = new Statistics(successStatisticsTwo, errorStatisticsTwo);

		final Statistics merged = statsOne.mergeWith(statsTwo);
		assertThat(merged.getErrorStatistics()).isEqualTo(errorStatisticsOne.mergeWith(errorStatisticsTwo));
		assertThat(merged.getSuccessStatistics()).isEqualTo(successStatisticsOne.mergeWith(successStatisticsTwo));
	}

	@Theory
	public void objectIdentity_equalsIsComposedOfAllFields(final SuccessStatistics successStatistics,
														   final ErrorStatistics errorStatistics) {
		final Statistics statsOne = new Statistics(successStatistics, errorStatistics);
		final Statistics statsTwo = new Statistics(successStatistics, errorStatistics);

		//Check that they don't just memeq
		assertThat(statsOne).isEqualTo(statsTwo);
		assertThat(statsOne).isEqualToComparingFieldByField(statsTwo);
	}

	@Theory
	public void objectIdentity_hashCodeIsEqualForEqualObjects(final SuccessStatistics successStatistics,
															  final ErrorStatistics errorStatistics) {
		final Statistics statsOne = new Statistics(successStatistics, errorStatistics);
		final Statistics statsTwo = new Statistics(successStatistics, errorStatistics);

		//Check that they don't just memeq
		assertThat(statsOne).isEqualTo(statsTwo);
		assertThat(statsOne.hashCode()).isEqualTo(statsTwo.hashCode());
	}
}