package wtf.emulator;

public enum ShardUnit {
  TEST_CLASSES("test_classes"),
  TEST_METHODS("test_methods");

  private final String cliValue;

  ShardUnit(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }
}
