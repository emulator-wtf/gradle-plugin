package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class TestCounts {
  public abstract int total();
  public abstract int passed();
  public abstract int failed();
  public abstract int timeout();
  public abstract int flaky();
  public abstract int skipped();

  public static Builder builder() {
    return new AutoValue_TestCounts.Builder()
        .total(0).passed(0).failed(0).timeout(0).flaky(0).skipped(0);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder total(int total);
    public abstract Builder passed(int passed);
    public abstract Builder failed(int failed);
    public abstract Builder timeout(int timeout);
    public abstract Builder flaky(int flaky);
    public abstract Builder skipped(int skipped);
    public abstract TestCounts build();
  }

  public static TypeAdapter<TestCounts> typeAdapter(Gson gson) {
    return new AutoValue_TestCounts.GsonTypeAdapter(gson);
  }
}
