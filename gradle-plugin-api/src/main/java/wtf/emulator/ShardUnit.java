package wtf.emulator;

public enum ShardUnit {
  /** Shard at the test class level; each shard receives whole test classes. */
  TEST_CLASSES("test_classes"),
  /** Shard at the test method level; individual test methods may be distributed across shards. */
  TEST_METHODS("test_methods");

  private final String cliValue;

  ShardUnit(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }
}
