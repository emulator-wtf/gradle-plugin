package wtf.emulator.exec;

import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.data.DeviceSpec;
import wtf.emulator.data.RunResult;
import wtf.emulator.data.RunResultsSummary;
import wtf.emulator.data.TestCaseResult;
import wtf.emulator.data.TestCounts;
import wtf.emulator.data.TestFailure;

public class CliOutputPrinter {
  public String getSummaryLines(EwCliOutput output) {
    StringBuilder sb = new StringBuilder();

    CliOutputSync sync = output.sync();
    CliOutputAsync async = output.async();

    if (sync == null) {
      if (async == null) {
        sb.append("❓ ");
        sb.append(output.displayName());
        sb.append("\n   Unknown test results, probably a bug. Reach out to us at support@emulator.wtf if you can read this.");
      } else if (output.exitCode() == 0) {
        sb.append("✅ ");
        sb.append(output.displayName());
        sb.append(" tests triggered successfully");
      } else {
        sb.append("❌ ");
        sb.append(output.displayName());
        sb.append(" failed to trigger tests");
      }

      return sb.toString();
    }

    RunResultsSummary summary = sync.runResultsSummary();

    RunResult result = summary != null && summary.runResult() != null ? summary.runResult() : guessRunResultFromExitCode(output.exitCode());

    sb.append(getResultEmoji(result));
    sb.append(" ");
    sb.append(output.displayName());

    if (sync.timeMs() != null || summary != null) {
      sb.append(" (");
      if (summary != null) {
        appendTestCounts(sb, summary.counts());
        if (sync.timeMs() != null) {
          sb.append(" in ");
        }
      }
      if (sync.timeMs() != null) {
        appendHumanReadableDuration(sb, sync.timeMs());
      }
      sb.append(")");
    }

    if (result != RunResult.SUCCESS) {
      if (summary != null) {
        if (summary.error() != null) {
          sb.append("\n   ");
          sb.append(summary.error());
        }
        else if (summary.firstFailure() != null) {
          sb.append("\n");
          appendIndentedFailure(sb, summary.firstFailure());
        }
      }
      sb.append("\n   More details: ");
      sb.append(sync.resultsUrl());
      sb.append("\n");
    }

    return sb.toString();
  }

  private static void appendIndentedFailure(StringBuilder sb, TestFailure failure) {
    sb.append("   ");
    sb.append(failure.className());
    sb.append(".");
    sb.append(failure.testName());
    sb.append(" ");
    sb.append(getTestCaseResultString(failure.result()));

    DeviceSpec spec = failure.deviceSpec();
    if (spec != null) {
      sb.append(" on ");
      sb.append(spec.model());
      sb.append(" ");
      sb.append(spec.api());
    }

    String failureBody = failure.failureBody();
    if (failureBody != null) {
      failureBody.lines().forEach((line) -> {
        sb.append("\n      ");
        sb.append(line);
      });
    }
  }

  private static void appendHumanReadableDuration(StringBuilder sb, long durationMs) {
    // for less than 30 seconds, show milliseconds
    if (durationMs < 30000) {
      sb.append(durationMs);
      sb.append("ms");
    } else {
      long seconds = durationMs / 1000;
      long minutes = seconds / 60;
      long hours = minutes / 60;

      if (hours > 0) {
        sb.append(hours);
        sb.append("h ");
      }
      if (minutes > 0) {
        sb.append(minutes % 60);
        sb.append("m ");
      }
      sb.append(seconds % 60);
      sb.append("s");
    }
  }

  private static String getTestCaseResultString(TestCaseResult caseResult) {
    switch (caseResult) {
    case TIMEOUT:
      return "timed out";
    case FAILURE:
      return "failed";
    case FLAKY:
      return "is flaky";
    case SUCCESS:
      return "passed";
    case SKIPPED:
      return "skipped";
    default:
      // TODO(madis) ?!?
      return "ran";
    }
  }

  private static RunResult guessRunResultFromExitCode(int exitCode) {
    switch (exitCode) {
    case 0:
      return RunResult.SUCCESS;
    case 10:
      return RunResult.FAIL;
    case 1:
    case 2:
    case 15:
    case 20:
    default:
      return RunResult.ERROR;
    }
  }

  private static String getResultEmoji(RunResult result) {
    switch (result) {
    case SUCCESS:
      return "✅";
    case FAIL:
      return "❌";
    case ERROR:
      return "💥";
    case CANCELED:
      return "⏸️";
    case TIMEOUT:
      return "⏰";
    case FLAKY:
      return "⚠️";
    default:
      return "❓";
    }
  }

  private static void appendTestCounts(StringBuilder sb, TestCounts counts) {
    int countsStart = sb.length();

    if (counts.total() > 0) {
      appendCount(sb, countsStart, counts.passed(), "passed");
      appendCount(sb, countsStart, counts.skipped(), "skipped");
      appendCount(sb, countsStart, counts.flaky(), "flaky");
      appendCount(sb, countsStart, counts.failed(), "failed");
      appendCount(sb, countsStart, counts.timeout(), "timed out");
    } else {
      sb.append("0");
    }

    sb.append(" / ");
    sb.append(counts.total());
    sb.append(" total");
  }

  private static void appendCount(StringBuilder builder, int countsStart, int count, String label) {
    if (count > 0) {
      if (builder.length() > countsStart) {
        builder.append(", ");
      }
      builder.append(count);
      builder.append(" ");
      builder.append(label);
    }
  }
}
