package wtf.emulator.exec;

import org.junit.Test;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.data.DeviceSpec;
import wtf.emulator.GpuMode;
import wtf.emulator.data.RunResult;
import wtf.emulator.data.RunResultsSummary;
import wtf.emulator.data.TestCaseResult;
import wtf.emulator.data.TestCounts;
import wtf.emulator.data.TestFailure;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

public class CliOutputPrinterTest {
  private static final String DISPLAY_NAME = ":app";
  private static final String TASK_PATH = ":app:testDebugWithEmulatorWtf";

  private final EwCliOutput.Builder builder = EwCliOutput.builder()
      .exitCode(0)
      .displayName(DISPLAY_NAME)
      .taskPath(TASK_PATH);

  private final CliOutputAsync asyncOut = CliOutputAsync.builder()
      .runToken("some-secret-token")
      .runUuid(UUID.randomUUID().toString())
      .startTime("2021-01-01T00:00:00Z")
      .build();

  private final CliOutputSync.Builder syncOutBuilder = CliOutputSync.builder()
      .timeMs(1234L)
      .billableMinutes(1)
      .resultsUrl("https://localhost/o/ooo/r/rrr?key=kkk")
      .testRunId(UUID.randomUUID().toString())
      .runResultsSummary(
          RunResultsSummary.builder()
              .runResult(RunResult.SUCCESS)
              .counts(TestCounts.builder()
                  .passed(11)
                  .skipped(3)
                  .total(14)
                  .build())
              .build()
      );

  @Test public void testBrokenOutputSuccess() {
    EwCliOutput output = builder.build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("❓ :app\n   Unknown test results, probably a bug. Reach out to us at support@emulator.wtf if you can read this.");
  }

  @Test public void testAsyncSuccess() {
    EwCliOutput output = builder.async(asyncOut).exitCode(0).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("✅ :app tests triggered successfully");
  }

  @Test public void testAsyncFailure() {
    EwCliOutput output = builder.async(asyncOut).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("❌ :app failed to trigger tests");
  }

  @Test public void testSyncSuccess() {
    CliOutputSync sync = syncOutBuilder.build();
    EwCliOutput output = builder.sync(sync).exitCode(0).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("✅ :app (11 passed, 3 skipped / 14 total in 1234ms)");
  }

  @Test public void noDuration() {
    CliOutputSync sync = syncOutBuilder.timeMs(null).build();
    EwCliOutput output = builder.sync(sync).exitCode(0).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("✅ :app (11 passed, 3 skipped / 14 total)");
  }

  @Test public void testMidDuration() {
    CliOutputSync sync = syncOutBuilder.timeMs(33678L).build();
    EwCliOutput output = builder.sync(sync).exitCode(0).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("✅ :app (11 passed, 3 skipped / 14 total in 33s)");
  }

  @Test public void testLongDuration() {
    CliOutputSync sync = syncOutBuilder.timeMs(12345678L).build();
    EwCliOutput output = builder.sync(sync).exitCode(0).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo("✅ :app (11 passed, 3 skipped / 14 total in 3h 25m 45s)");
  }

  @Test public void testFailureNoSummary() {
    CliOutputSync sync = syncOutBuilder.runResultsSummary(null).build();
    EwCliOutput output = builder.sync(sync).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo(
        "💥 :app (1234ms)\n" +
            "   More details: https://localhost/o/ooo/r/rrr?key=kkk\n"
    );
  }

  @Test public void testError() {
    CliOutputSync sync = syncOutBuilder.runResultsSummary(
      RunResultsSummary.builder()
          .runResult(RunResult.ERROR)
          .error("No tests!")
          .counts(TestCounts.builder().build())
          .build()
    ).build();
    EwCliOutput output = builder.sync(sync).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo(
        "💥 :app (0 / 0 total in 1234ms)\n" +
            "   No tests!\n" +
            "   More details: https://localhost/o/ooo/r/rrr?key=kkk\n"
    );
  }

  @Test public void testFailed() {
    CliOutputSync sync = syncOutBuilder.runResultsSummary(
        RunResultsSummary.builder()
            .runResult(RunResult.FAIL)
            .counts(TestCounts.builder().passed(3).failed(1).total(4).build())
            .firstFailure(
                TestFailure.builder()
                    .className("com.example.FooTest")
                    .testName("testBar")
                    .failureBody("java.lang.AssertionError: Expected 1 but was 2")
                    .result(TestCaseResult.FAILURE)
                    .jobUuid("job-uuid")
                    .deviceSpec(
                        DeviceSpec.builder().model("Monitor").api(35).gpuMode(GpuMode.auto).build()
                    )
                    .build()
            )
            .build()
    ).build();
    EwCliOutput output = builder.sync(sync).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo(
        "❌ :app (3 passed, 1 failed / 4 total in 1234ms)\n" +
            "   com.example.FooTest.testBar failed on Monitor 35\n" +
            "      java.lang.AssertionError: Expected 1 but was 2\n" +
            "   More details: https://localhost/o/ooo/r/rrr?key=kkk\n"
    );
  }

  @Test public void testFailedMultiline() {
    CliOutputSync sync = syncOutBuilder.runResultsSummary(
        RunResultsSummary.builder()
            .runResult(RunResult.FAIL)
            .counts(TestCounts.builder().passed(3).failed(1).total(4).build())
            .firstFailure(
                TestFailure.builder()
                    .className("FailingTests")
                    .testName("willFail")
                    .failureBody("java.lang.AssertionError\nat org.junit.Assert.fail(Assert.java:87)\nat org.junit.Assert.assertTrue(Assert.java:42)\nat org.junit.Assert.assertTrue(Assert.java:53)\nat FailingTests.willFail(FailingTests.java:8)")
                    .result(TestCaseResult.FAILURE)
                    .jobUuid("job-uuid")
                    .deviceSpec(
                        DeviceSpec.builder().model("Monitor").api(35).gpuMode(GpuMode.auto).build()
                    )
                    .build()
            )
            .build()
    ).build();
    EwCliOutput output = builder.sync(sync).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo(
        "❌ :app (3 passed, 1 failed / 4 total in 1234ms)\n" +
            "   FailingTests.willFail failed on Monitor 35\n" +
            "      java.lang.AssertionError\n" +
            "      at org.junit.Assert.fail(Assert.java:87)\n" +
            "      at org.junit.Assert.assertTrue(Assert.java:42)\n" +
            "      at org.junit.Assert.assertTrue(Assert.java:53)\n" +
            "      at FailingTests.willFail(FailingTests.java:8)\n" +
            "   More details: https://localhost/o/ooo/r/rrr?key=kkk\n"
    );
  }

  @Test public void testFlaky() {
    CliOutputSync sync = syncOutBuilder.runResultsSummary(
        RunResultsSummary.builder()
            .runResult(RunResult.FLAKY)
            .counts(TestCounts.builder().passed(3).flaky(1).total(4).build())
            .build()
    ).build();
    EwCliOutput output = builder.sync(sync).exitCode(1).build();

    String summaryLine = new CliOutputPrinter().getSummaryLines(output);

    assertThat(summaryLine).isEqualTo(
        "⚠️ :app (3 passed, 1 flaky / 4 total in 1234ms)\n" +
            "   More details: https://localhost/o/ooo/r/rrr?key=kkk\n"
    );
  }
}
