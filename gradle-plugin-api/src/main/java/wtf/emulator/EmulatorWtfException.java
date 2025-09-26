package wtf.emulator;

import org.gradle.api.GradleException;

/**
 * Thrown when ew-cli execution fails. This does not necessary indicate an error,
 * it can also be thrown when there are failing tests.
 */
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
