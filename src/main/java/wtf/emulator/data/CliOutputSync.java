package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;

@AutoValue public abstract class CliOutputSync {
  public abstract String testRunId();
  public abstract long timeMs();
  @Nullable public abstract Integer billableMinutes();
  @Nullable public abstract String resultsUrl();
  @Nullable public abstract RunResultsSummary runResultsSummary();

  public static TypeAdapter<CliOutputSync> typeAdapter(Gson gson) {
    return new AutoValue_CliOutputSync.GsonTypeAdapter(gson);
  }
}
