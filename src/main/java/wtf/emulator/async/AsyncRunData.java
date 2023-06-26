package wtf.emulator.async;

import java.util.Objects;

public class AsyncRunData {
  private final String runUuid;
  private final String runToken;
  private final String startTime;
  private final String displayName;

  public AsyncRunData(String runUuid, String runToken, String startTime, String displayName) {
    this.runUuid = runUuid;
    this.runToken = runToken;
    this.startTime = startTime;
    this.displayName = displayName;
  }

  public String getRunUuid() {
    return runUuid;
  }

  public String getRunToken() {
    return runToken;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AsyncRunData that = (AsyncRunData) o;
    return Objects.equals(runUuid, that.runUuid) && Objects.equals(runToken, that.runToken) && Objects.equals(startTime, that.startTime) && Objects.equals(displayName, that.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runUuid, runToken, startTime, displayName);
  }
}
