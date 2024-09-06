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

  public static TypeAdapter<TestFailure> typeAdapter(Gson gson) {
    return new AutoValue_TestFailure.GsonTypeAdapter(gson);
  }
}
