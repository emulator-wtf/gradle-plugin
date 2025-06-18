package wtf.emulator;

/**
 * Device model (screen size, density) to use for executing tests.
 */
public enum DeviceModel {
  PIXEL_2("Pixel2", 420),
  PIXEL_2_ATD("Pixel2Atd", 420),
  PIXEL_7("Pixel7", 420),
  PIXEL_7_ATD("Pixel7Atd", 420),
  TABLET_10("Tablet10", 240),
  TABLET_10_ATD("Tablet10Atd", 240),
  NEXUS_LOW_RES("NexusLowRes", 160),
  NEXUS_LOW_RES_ATD("NexusLowResAtd", 160),
  MONITOR("Monitor", 213);

  private final String cliValue;
  private final int density;

  DeviceModel(String cliValue, int density) {
    this.cliValue = cliValue;
    this.density = density;
  }

  public String getCliValue() {
    return cliValue;
  }

  public int getDensity() {
    return density;
  }
}
