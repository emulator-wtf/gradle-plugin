package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;

@AutoValue public abstract class CliOutputSync {
  public abstract String testRunId();
  @Nullable public abstract Long timeMs();
  @Nullable public abstract Integer billableMinutes();
  @Nullable public abstract String resultsUrl();
  @Nullable public abstract RunResultsSummary runResultsSummary();

  public static Builder builder() {
    return new AutoValue_CliOutputSync.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder testRunId(String testRunId);
    public abstract Builder timeMs(@Nullable Long timeMs);
    public abstract Builder billableMinutes(@Nullable Integer billableMinutes);
    public abstract Builder resultsUrl(@Nullable String resultsUrl);
    public abstract Builder runResultsSummary(@Nullable RunResultsSummary runResultsSummary);
    public abstract CliOutputSync build();
  }

  public static TypeAdapter<CliOutputSync> typeAdapter(Gson gson) {
    return new AutoValue_CliOutputSync.GsonTypeAdapter(gson);
  }
}
