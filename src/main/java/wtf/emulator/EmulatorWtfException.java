package wtf.emulator;

import org.gradle.api.GradleException;

public class EmulatorWtfException extends GradleException {
  public EmulatorWtfException() {
    super();
  }

  public EmulatorWtfException(String message) {
    super(message);
  }

  public EmulatorWtfException(String message, Throwable cause) {
    super(message, cause);
  }
}
