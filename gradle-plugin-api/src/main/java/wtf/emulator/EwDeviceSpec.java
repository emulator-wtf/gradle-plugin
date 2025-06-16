package wtf.emulator;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Specific device configuration to run tests on.
 */
public abstract class EwDeviceSpec implements Serializable {
  public static final DeviceModel DEFAULT_MODEL = DeviceModel.PIXEL_7;
  public static final int DEFAULT_VERSION = 30;
  public static final GpuMode DEFAULT_GPU = GpuMode.AUTO;

  public abstract Property<DeviceModel> getModel();
  public abstract Property<Integer> getVersion();
  public abstract Property<GpuMode> getGpu();

  private Provider<DeviceModel> model() {
    return getModel().orElse(DEFAULT_MODEL);
  }

  private Provider<Integer> version() {
    return getVersion().orElse(DEFAULT_VERSION);
  }

  private Provider<GpuMode> gpu() {
    return getGpu().orElse(DEFAULT_GPU);
  }

  public Provider<Map<String, String>> toCliMap() {
    return model().flatMap(model -> version().flatMap(version -> gpu().map(gpu -> {
      Map<String, String> map = new HashMap<>();
      map.put("model", model.getCliValue());
      map.put("version", Integer.toString(version));
      map.put("gpu", gpu.getCliValue());
      return map;
    })));
  }
}
