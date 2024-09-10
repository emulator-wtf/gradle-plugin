package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;

@AutoValue public abstract class TestFailure {
  public abstract String jobUuid();
  public abstract String className();
  public abstract String testName();
  public abstract TestCaseResult result();
  @Nullable public abstract DeviceSpec deviceSpec();
  @Nullable public abstract Integer shardIndex();
  @Nullable public abstract String failureBody();

  public static Builder builder() {
    return new AutoValue_TestFailure.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder jobUuid(String jobUuid);
    public abstract Builder className(String className);
    public abstract Builder testName(String testName);
    public abstract Builder result(TestCaseResult result);
    public abstract Builder deviceSpec(@Nullable DeviceSpec deviceSpec);
    public abstract Builder shardIndex(@Nullable Integer shardIndex);
    public abstract Builder failureBody(@Nullable String failureBody);
    public abstract TestFailure build();
  }

  public static TypeAdapter<TestFailure> typeAdapter(Gson gson) {
    return new AutoValue_TestFailure.GsonTypeAdapter(gson);
  }
}
