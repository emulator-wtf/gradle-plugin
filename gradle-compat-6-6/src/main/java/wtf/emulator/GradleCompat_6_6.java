package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

public class GradleCompat_6_6 extends GradleCompat_6_5 {
  public GradleCompat_6_6(Gradle gradle) {
    super(gradle);
  }

  @Override
  public boolean isConfigurationCacheEnabled() {
    return ((StartParameterInternal) gradle.getStartParameter()).isConfigurationCache();
  }

  @Override
  public void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider) {
    project.getDependencies().add(configurationName, notationProvider);
  }
}
