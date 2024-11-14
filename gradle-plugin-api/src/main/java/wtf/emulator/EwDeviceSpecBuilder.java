package wtf.emulator;

/**
 * Specific device configuration to run tests on.
 */
public class EwDeviceSpecBuilder {
  private DeviceModel model = DeviceModel.PIXEL_7;
  private int version = 30;
  private GpuMode gpu = GpuMode.AUTO;

  /**
   * Sets the device model to use, see {@link DeviceModel} or
   * <a href="https://docs.emulator.wtf/emulators/">https://docs.emulator.wtf/emulators/</a> for available values.
   */
  public EwDeviceSpecBuilder model(DeviceModel model) {
    this.model = model;
    return this;
  }

  /**
   * Sets the Android API level to use. Use {@code ew-cli --models} or see
   * <a href="https://docs.emulator.wtf/emulators/">https://docs.emulator.wtf/emulators/</a> for available
   * model x version combinations.
   */
  public EwDeviceSpecBuilder version(int apiLevel) {
    this.version = apiLevel;
    return this;
  }

  /**
   * Sets the GPU mode to use. NOTE: when using {@link GpuMode#AUTO} there's no guarantee that the emulator will
   * actually use GPU acceleration in rare cases.
   *
   * @param gpuMode either {@link GpuMode#SOFTWARE} or {@link GpuMode#AUTO} (default).
   * @return
   */
  public EwDeviceSpecBuilder gpu(GpuMode gpuMode) {
    this.gpu = gpuMode;
    return this;
  }

  public EwDeviceSpec build() {
    return new EwDeviceSpec(model, version, gpu);
  }
}
