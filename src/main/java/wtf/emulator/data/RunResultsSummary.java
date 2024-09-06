package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;

@AutoValue public abstract class RunResultsSummary {
  public abstract RunResult runResult();
  public abstract TestCounts counts();
  @Nullable public abstract String error();
  @Nullable public abstract TestFailure firstFailure();

  public static TypeAdapter<RunResultsSummary> typeAdapter(Gson gson) {
    return new AutoValue_RunResultsSummary.GsonTypeAdapter(gson);
  }
}
