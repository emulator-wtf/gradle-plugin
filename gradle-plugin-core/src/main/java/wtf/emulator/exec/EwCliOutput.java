package wtf.emulator.exec;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.data.CliOutputSync;
import wtf.emulator.data.RunResult;

import javax.annotation.Nullable;
import java.util.EnumSet;

@AutoValue public abstract class EwCliOutput {
  @Nullable public abstract CliOutputAsync async();
  @Nullable public abstract CliOutputSync sync();
  @Nullable public abstract String displayName();
  public abstract String taskPath();
  public abstract int exitCode();

  private static final EnumSet<RunResult> SUCCESSY_RESULTS = EnumSet.of(RunResult.SUCCESS, RunResult.FLAKY, RunResult.CANCELED);

  public boolean isSuccess() {
    if (exitCode() != 0) {
      return false;
    }

    if (sync() == null) {
      return true;
    }

    if (sync().runResultsSummary() == null) {
      return true;
    }

    return SUCCESSY_RESULTS.contains(sync().runResultsSummary().runResult());
  }

  public boolean isFailure() {
    return !isSuccess();
  }

  /**
   * @return true if the output represents a test failure (only sync), false otherwise (including non-test errors, async outputs, and successes)
   */
  public boolean isTestFailure() {
    if (sync() == null) {
      return false;
    }

    if (sync().runResultsSummary() == null) {
      return false;
    }

    return sync().runResultsSummary().runResult() == RunResult.FAIL;
  }

  // Used for JSON deserialization only, code callers should be using the specific builders below
  static Builder builder() {
    return new AutoValue_EwCliOutput.Builder();
  }

  public static Builder builder(CliOutputAsync output) {
    return builder().async(output);
  }

  public static Builder builder(CliOutputSync output) {
    return builder().sync(output);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder async(CliOutputAsync async);
    public abstract Builder sync(CliOutputSync sync);
    public abstract Builder displayName(String displayName);
    public abstract Builder taskPath(String taskPath);
    public abstract Builder exitCode(int exitCode);
    public abstract EwCliOutput build();
  }

  public static TypeAdapter<EwCliOutput> typeAdapter(Gson gson) {
    return new AutoValue_EwCliOutput.GsonTypeAdapter(gson);
  }
}
