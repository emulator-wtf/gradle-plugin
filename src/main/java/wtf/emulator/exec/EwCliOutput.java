package wtf.emulator.exec;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.data.CliOutputSync;

import javax.annotation.Nullable;

@AutoValue public abstract class EwCliOutput {
  @Nullable public abstract CliOutputAsync async();
  @Nullable public abstract CliOutputSync sync();

  public static EwCliOutput create(CliOutputSync output) {
    return new AutoValue_EwCliOutput(null, output);
  }

  public static EwCliOutput create(CliOutputAsync output) {
    return new AutoValue_EwCliOutput(output, null);
  }

  public static TypeAdapter<EwCliOutput> typeAdapter(Gson gson) {
    return new AutoValue_EwCliOutput.GsonTypeAdapter(gson);
  }
}
