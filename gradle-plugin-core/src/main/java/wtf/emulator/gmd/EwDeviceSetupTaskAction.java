package wtf.emulator.gmd;

import com.android.build.api.instrumentation.manageddevice.DeviceSetupTaskAction;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public abstract class EwDeviceSetupTaskAction implements DeviceSetupTaskAction<EwDeviceSetupInput> {

  @Override
  public void setup(@NotNull EwDeviceSetupInput ewDeviceSetupInput, @NotNull Directory directory) {
    // Bust the test run task cache by writing a random value to a file that's used as an input for the task.
    if (ewDeviceSetupInput.getCacheEnabled().get()) {
      Path filePath = directory.file("cachebust.txt").getAsFile().toPath();
      try {
        Files.writeString(filePath, String.valueOf(Math.random()));
      } catch (IOException e) {
        Logging.getLogger(EwDeviceSetupTaskAction.class)
          .warn("Failed to write cache bust file", e);
      }
    }

  }
}
