package wtf.emulator;

/**
 * Specific device configuration to run tests on.
 */
public class EwDeviceSpecBuilder {
  private EwDeviceModel model = EwDeviceModel.PIXEL_7;
  private int version = 30;
  private EwGpuMode gpu = EwGpuMode.AUTO;

  /**
   * Sets the device model to use, see {@link EwDeviceModel} or
   * <a href="https://docs.emulator.wtf/emulators/">https://docs.emulator.wtf/emulators/</a> for available values.
   */
  public EwDeviceSpecBuilder model(EwDeviceModel model) {
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
   * Sets the GPU mode to use. NOTE: when using {@link EwGpuMode#AUTO} there's no guarantee that the emulator will
   * actually use GPU acceleration in rare cases.
   *
   * @param gpuMode either {@link EwGpuMode#SOFTWARE} or {@link EwGpuMode#AUTO} (default).
   * @return
   */
  public EwDeviceSpecBuilder gpu(EwGpuMode gpuMode) {
    this.gpu = gpuMode;
    return this;
  }

  public EwDeviceSpec build() {
    return new EwDeviceSpec(model, version, gpu);
  }
}
