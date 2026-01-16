package wtf.emulator;

import org.gradle.api.provider.Property;

import java.io.Serializable;

/**
 * Specific device configuration to run tests on.
 */
public abstract class EwDeviceSpec implements Serializable {
  public static final DeviceModel DEFAULT_MODEL = DeviceModel.PIXEL_7;
  public static final int DEFAULT_VERSION = 30;
  public static final GpuMode DEFAULT_GPU = GpuMode.AUTO;

  public abstract Property<DeviceModel> getModel();
  public abstract Property<Integer> getVersion();
  public abstract Property<String> getLocale();
  public abstract Property<GpuMode> getGpu();
}
