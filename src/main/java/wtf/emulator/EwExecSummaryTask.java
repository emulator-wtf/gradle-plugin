package wtf.emulator;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EwExecSummaryTask extends DefaultTask {
  @InputFiles
  public abstract SetProperty<File> getFailureMessages();

  @Input
  @Optional
  public abstract Property<Boolean> getPrintingEnabled();

  @TaskAction
  public void exec() {
    final Boolean printingEnabled = getPrintingEnabled().getOrElse(false);
    if (printingEnabled) {
      final List<String> failureMessages = getFailureMessages().get()
          .stream().map((file) -> {
            try {
              if (file.exists()) {
                return FileUtils.readFileToString(file, "UTF-8");
              }
            } catch (Exception e) {
              /* ignore */
            }
            return "";
          })
          .filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());

      if (!failureMessages.isEmpty()) {
        getLogger().warn("There were emulator.wtf test failures:");
        for (String message : failureMessages) {
          if (message == null || message.isEmpty()) {
            continue;
          }
          getLogger().warn(message);
        }
      }
    }
  }
}
