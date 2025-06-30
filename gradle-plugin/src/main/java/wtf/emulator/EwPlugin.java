package wtf.emulator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wtf.emulator.setup.ProjectConfigurator;


@SuppressWarnings("unused")
public class EwPlugin implements Plugin<Project> {

  @Override
  public void apply(Project target) {
    EwExtension ext = target.getExtensions().create("emulatorwtf", EwExtension.class);

    ProjectConfigurator projectConfig = new ProjectConfigurator(target, ext, new EwExtensionInternal(ext));
    projectConfig.configure();
  }
}
