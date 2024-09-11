package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class AgpOutputMetadataVersion {
  public abstract int version();

  public static Builder builder() {
    return new AutoValue_AgpOutputMetadataVersion.Builder();
  }

  public static TypeAdapter<AgpOutputMetadataVersion> typeAdapter(Gson gson) {
    return new AutoValue_AgpOutputMetadataVersion.GsonTypeAdapter(gson);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder version(int version);
    public abstract AgpOutputMetadataVersion build();
  }
}
