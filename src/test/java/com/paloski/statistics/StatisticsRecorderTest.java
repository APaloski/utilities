package com.paloski.statistics;

import org.assertj.core.description.TextDescription;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(Theories.class)
public final class StatisticsRecorderTest {

	public static final String DATA_POINTS__SUCCESS_COUNT = "Data-Points::Success-Count";
	public static final String DATA_POINTS__ERROR_COUNT = "Data-Points::Error-Count";

	public static final String DATA_POINTS__THREADING__THREAD_COUNT = "Data-Points::Threading::Thread-Count";
	public static final String DATA_POINTS__THREADING__SUCCESS_COUNT = "Data-Points::Threading::Success-Count";
	public static final String DATA_POINTS__THREADING__ERROR_COUNT = "Data-Points::Threading::Error-Count";

	@DataPoints(DATA_POINTS__SUCCESS_COUNT)
	public static List<Long> getSuccessRecordingCounts() {
		return Arrays.asList(0L, 1L, 2L);
	}

	@DataPoints(DATA_POINTS__ERROR_COUNT)
	public static List<Long> getErrorRecordingCounts() {
		return Arrays.asList(0L, 1L, 2L);
	}

	@DataPoints(DATA_POINTS__THREADING__THREAD_COUNT)
	public static List<Integer> getThreadCount() {
		return Arrays.asList(1, 2, 5, 10, 20, 30);
	}

	@DataPoints(DATA_POINTS__THREADING__SUCCESS_COUNT)
	public static List<Long> getThreadedSuccessRecordingCounts() {
		return Arrays.asList(0L, 3000L, 10000L);
	}

	@DataPoints(DATA_POINTS__THREADING__ERROR_COUNT)
	public static List<Long> getThreadedErrorRecordingCounts() {
		return Arrays.asList(0L, 3000L, 10000L);
	}

	@Theory
	public void recordSuccess_snapshotReflectsRecording(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
														@FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics snapshot = sut.takeSnapshot();
		assertThat(snapshot.getSuccessCount()).isEqualTo(successCount);
	}

	@Theory
	public void recordSuccess_doesNotAlterExistingSnapshot(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
														   @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics snapshot = sut.takeSnapshot();
		final long beforeSuccessCount = snapshot.getSuccessCount();
		sut.recordSuccess();
		assertThat(snapshot.getSuccessCount()).isEqualTo(beforeSuccessCount);
	}

	@Theory
	public void recordSuccess_doesNotAlterErrorCount(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
													 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		sut.recordSuccess();
		final Statistics snapshot = sut.takeSnapshot();
		assertThat(snapshot.getErrorCount()).isEqualTo(errorCount);
	}

	@Theory
	public void recordError_snapshotReflectsRecording(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
													  @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics snapshot = sut.takeSnapshot();
		assertThat(snapshot.getErrorCount()).isEqualTo(errorCount);
	}

	@Theory
	public void recordError_doesNotAlterExistingSnapshot(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
														 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics snapshot = sut.takeSnapshot();
		final long beforeErrorCount = snapshot.getErrorCount();
		sut.recordError(new Exception());
		assertThat(snapshot.getErrorCount()).isEqualTo(beforeErrorCount);
	}

	@Theory
	public void recordError_doesNotAlterSuccessCount(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
													 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < successCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < errorCount; count++) {
			sut.recordError(new Exception());
		}

		sut.recordError(new Exception());
		final Statistics snapshot = sut.takeSnapshot();
		assertThat(snapshot.getSuccessCount()).isEqualTo(successCount);
	}

	@Theory
	public void takeSnapshot_snapshotsAfterInitialReflectChanges(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long firstRunSuccessCount,
																 @FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long secondRunSuccessCount,
																 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long firstRunErrorCount,
																 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long secondRunErrorCount) {
		final StatisticsRecorder sut = StatisticsRecorder.newRecorder();
		for (int count = 0; count < firstRunSuccessCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < firstRunErrorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics initialSnapshot = sut.takeSnapshot();

		for (int count = 0; count < secondRunSuccessCount; count++) {
			sut.recordSuccess();
		}

		for (int count = 0; count < secondRunErrorCount; count++) {
			sut.recordError(new Exception());
		}

		final Statistics secondarySnapshot = sut.takeSnapshot();
		assertThat(secondarySnapshot.getSuccessCount()).isEqualTo(firstRunSuccessCount + secondRunSuccessCount);
		assertThat(secondarySnapshot.getErrorCount()).isEqualTo(firstRunErrorCount + secondRunErrorCount);
	}

	@Theory
	public void newRecorder_newRecorderStartsEmpty() {
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		final Statistics stats = recorder.takeSnapshot();
		assertThat(stats.getSuccessCount()).isEqualTo(0L);
		assertThat(stats.getErrorCount()).isEqualTo(0L);
	}

	@Theory
	public void seededRecorder_seededRecorderStartsWithSeedValue(@FromDataPoints(DATA_POINTS__SUCCESS_COUNT) final long successCount,
																 @FromDataPoints(DATA_POINTS__ERROR_COUNT) final long errorCount) {
		final Statistics seed = new Statistics(SuccessStatistics.forSuccessCount(successCount),
											   ErrorStatistics.forFailureCount(errorCount));
		final StatisticsRecorder seeded = StatisticsRecorder.newSeededRecorder(seed);
		assertThat(seeded.takeSnapshot()).isEqualTo(seed);
	}

	@Theory
	public void threadSafety_threadedRecordingCreatesCorrectCount(@FromDataPoints(DATA_POINTS__THREADING__SUCCESS_COUNT) final long successCount,
																  @FromDataPoints(DATA_POINTS__THREADING__ERROR_COUNT) final long errorCount,
																  @FromDataPoints(DATA_POINTS__THREADING__THREAD_COUNT) final int threadCount) throws InterruptedException {
		final ExecutorService service = Executors.newFixedThreadPool(threadCount);
		final StatisticsRecorder recorder = StatisticsRecorder.newRecorder();
		try {
			for (long x = 0; x < successCount; x++) {
				service.execute(new Runnable() {
					@Override
					public void run() {
						recorder.recordSuccess();
					}
				});
			}

			for (long x = 0; x < errorCount; x++) {
				service.execute(new Runnable() {
					@Override
					public void run() {
						recorder.recordError(new Exception());
					}
				});
			}

			service.shutdown();
			assertThat(service.awaitTermination(1, TimeUnit.MINUTES)).as(new TextDescription("Executor did not shutdown in the acceptable amount of time"))
																	 .isTrue();
			final Statistics stats = recorder.takeSnapshot();
			assertThat(stats.getErrorCount()).isEqualTo(errorCount);
			assertThat(stats.getSuccessCount()).isEqualTo(successCount);

		} finally {
			service.shutdownNow();
		}

	}
}