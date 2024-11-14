package wtf.emulator;

/**
 * Device model (screen size, density) to use for executing tests.
 */
public enum DeviceModel {
  PIXEL_2("Pixel2"),
  PIXEL_2_ATD("Pixel2Atd"),
  PIXEL_7("Pixel7"),
  PIXEL_7_ATD("Pixel7Atd"),
  TABLET_10("Tablet10"),
  TABLET_10_ATD("Tablet10Atd"),
  NEXUS_LOW_RES("NexusLowRes"),
  NEXUS_LOW_RES_ATD("NexusLowResAtd"),
  MONITOR("Monitor");

  private final String cliValue;

  DeviceModel(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }
}
