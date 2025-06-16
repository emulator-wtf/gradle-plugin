package wtf.emulator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Specific device configuration to run tests on.
 */
public record EwDeviceSpec(DeviceModel model, int version, GpuMode gpu) implements Serializable {
  public static EwDeviceSpecBuilder builder() {
    return new EwDeviceSpecBuilder();
  }

  public Map<String, String> toCliMap() {
    var map = new HashMap<String, String>();
    map.put("model", model.getCliValue());
    map.put("version", Integer.toString(version));
    map.put("gpu", gpu.getCliValue());
    return map;
  }

  public EwDeviceSpecBuilder toBuilder() {
    return builder().model(model).version(version).gpu(gpu);
  }
}
