package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class AgpOutputMetadata {
  public abstract int version();
  public abstract ArtifactType artifactType();
  public abstract String applicationId();
  public abstract String variantName();
  public abstract List<Element> elements();
  public abstract String elementType();

  public static Builder builder() {
    return new AutoValue_AgpOutputMetadata.Builder();
  }

  public static TypeAdapter<AgpOutputMetadata> typeAdapter(Gson gson) {
    return new AutoValue_AgpOutputMetadata.GsonTypeAdapter(gson);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder version(int version);
    public abstract Builder artifactType(ArtifactType artifactType);
    public abstract Builder applicationId(String applicationId);
    public abstract Builder variantName(String variantName);
    public abstract Builder elements(List<Element> elements);
    public abstract Builder elementType(String elementType);
    public abstract AgpOutputMetadata build();
  }

  @AutoValue
  public abstract static class ArtifactType {
    public abstract String type();
    public abstract String kind();

    public static Builder builder() {
      return new AutoValue_AgpOutputMetadata_ArtifactType.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder type(String type);
      public abstract Builder kind(String kind);
      public abstract ArtifactType build();
    }

    public static TypeAdapter<ArtifactType> typeAdapter(Gson gson) {
      return new AutoValue_AgpOutputMetadata_ArtifactType.GsonTypeAdapter(gson);
    }
  }

  @AutoValue
  public abstract static class Element {
    public abstract String type();
    public abstract List<FilterElement> filters();
    public abstract List<KeyValuePair> attributes();
    public abstract int versionCode();
    public abstract String versionName();
    public abstract String outputFile();

    public static Builder builder() {
      return new AutoValue_AgpOutputMetadata_Element.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder type(String type);
      public abstract Builder filters(List<FilterElement> filters);
      public abstract Builder attributes(List<KeyValuePair> attributes);
      public abstract Builder versionCode(int versionCode);
      public abstract Builder versionName(String versionName);
      public abstract Builder outputFile(String outputFile);
      public abstract Element build();
    }

    public static TypeAdapter<Element> typeAdapter(Gson gson) {
      return new AutoValue_AgpOutputMetadata_Element.GsonTypeAdapter(gson);
    }
  }

  @AutoValue
  public abstract static class FilterElement {
    public abstract String filterType();
    public abstract String value();

    public static FilterElement.Builder builder() {
      return new AutoValue_AgpOutputMetadata_FilterElement.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract FilterElement.Builder filterType(String filterType);
      public abstract FilterElement.Builder value(String value);
      public abstract FilterElement build();
    }

    public static TypeAdapter<FilterElement> typeAdapter(Gson gson) {
      return new AutoValue_AgpOutputMetadata_FilterElement.GsonTypeAdapter(gson);
    }
  }

  @AutoValue
  public abstract static class KeyValuePair {
    public abstract String key();
    public abstract String value();

    public static KeyValuePair.Builder builder() {
      return new AutoValue_AgpOutputMetadata_KeyValuePair.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract KeyValuePair.Builder key(String type);
      public abstract KeyValuePair.Builder value(String value);
      public abstract KeyValuePair build();
    }

    public static TypeAdapter<KeyValuePair> typeAdapter(Gson gson) {
      return new AutoValue_AgpOutputMetadata_KeyValuePair.GsonTypeAdapter(gson);
    }
  }
}
