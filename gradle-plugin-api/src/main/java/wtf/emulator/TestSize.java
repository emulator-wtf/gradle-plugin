package wtf.emulator;

import java.io.Serializable;

public enum TestSize implements Serializable {
  SMALL("small"),
  MEDIUM("medium"),
  LARGE("large");

  private String cliValue;

  TestSize(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }

  @Override
  public String toString() {
    return cliValue;
  }
}
