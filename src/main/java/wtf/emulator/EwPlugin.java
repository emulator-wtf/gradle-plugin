package wtf.emulator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wtf.emulator.data.EwTypeAdapterFactory;
import wtf.emulator.setup.ProjectConfigurator;


@SuppressWarnings("unused")
public class EwPlugin implements Plugin<Project> {

  // single global Gson instance for all EW stuff
  public static final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(EwTypeAdapterFactory.create())
      .setPrettyPrinting()
      .create();

  @Override
  public void apply(Project target) {
    EwExtension ext = target.getExtensions().create("emulatorwtf", EwExtension.class);
    GradleCompat gradleCompat = GradleCompatFactory.get(target.getGradle());

    ProjectConfigurator projectConfig = new ProjectConfigurator(target, ext, gradleCompat);
    projectConfig.configure();
  }
}
