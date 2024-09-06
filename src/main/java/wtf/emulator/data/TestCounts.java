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

  public static TypeAdapter<TestCounts> typeAdapter(Gson gson) {
    return new AutoValue_TestCounts.GsonTypeAdapter(gson);
  }
}
