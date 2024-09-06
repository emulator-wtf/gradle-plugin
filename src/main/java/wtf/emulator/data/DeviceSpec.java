package wtf.emulator.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class DeviceSpec {
  public abstract String model();
  public abstract int api();
  public abstract GpuMode gpuMode();

  public static TypeAdapter<DeviceSpec> typeAdapter(Gson gson) {
    return new AutoValue_DeviceSpec.GsonTypeAdapter(gson);
  }
}
