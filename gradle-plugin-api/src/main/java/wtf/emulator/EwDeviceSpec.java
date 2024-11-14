package wtf.emulator;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Specific device configuration to run tests on.
 */
public class EwDeviceSpec implements Serializable {
  private final EwDeviceModel model;
  private final int version;
  private final EwGpuMode gpu;

  public static EwDeviceSpecBuilder builder() {
    return new EwDeviceSpecBuilder();
  }

  public EwDeviceSpec(EwDeviceModel model, int version, EwGpuMode gpu) {
    this.model = model;
    this.version = version;
    this.gpu = gpu;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EwDeviceSpec that = (EwDeviceSpec) o;
    return version == that.version && model == that.model && gpu == that.gpu;
  }

  @Override
  public int hashCode() {
    return Objects.hash(model, version, gpu);
  }

  public Map<String, String> toCliMap() {
    return Map.of(
        "model", model.getCliValue(),
        "version", Integer.toString(version),
        "gpu", gpu.getCliValue()
    );
  }

  public EwDeviceSpecBuilder toBuilder() {
    return builder().model(model).version(version).gpu(gpu);
  }
}
