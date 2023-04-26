package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public class GradleCompat_6_1 implements GradleCompat {
  @Override
  public boolean isConfigurationCacheEnabled() {
    // config cache starting from 6.6
    return false;
  }

  @Override
  public void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider) {
    // old Gradle versions don't support Provider<String>, loop through afterEvaluate instead :(
    project.afterEvaluate(it ->
      it.getDependencies().add(configurationName, notationProvider.get())
    );
  }
}
