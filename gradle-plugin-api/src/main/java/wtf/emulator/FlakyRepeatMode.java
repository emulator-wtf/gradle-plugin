package wtf.emulator;

public enum FlakyRepeatMode {
  ALL("all"),
  FAILED_ONLY("failed_only");

  private final String cliValue;

  FlakyRepeatMode(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }
}
