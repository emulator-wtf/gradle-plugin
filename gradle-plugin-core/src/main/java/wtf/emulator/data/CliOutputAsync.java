package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class CliOutputAsync {
  public abstract String runUuid();
  public abstract String runToken();
  public abstract String startTime();

  public static Builder builder() {
    return new AutoValue_CliOutputAsync.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder runUuid(String runUuid);
    public abstract Builder runToken(String runToken);
    public abstract Builder startTime(String startTime);
    public abstract CliOutputAsync build();
  }

  public static TypeAdapter<CliOutputAsync> typeAdapter(Gson gson) {
    return new AutoValue_CliOutputAsync.GsonTypeAdapter(gson);
  }
}
