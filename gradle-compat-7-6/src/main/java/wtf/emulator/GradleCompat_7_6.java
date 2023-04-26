package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_7_6 implements GradleCompat {
  private final Gradle gradle;

  public GradleCompat_7_6(Gradle gradle) {
    this.gradle = gradle;
  }

  @Override
  public boolean isConfigurationCacheEnabled() {
    return gradle.getStartParameter().isConfigurationCacheRequested();
  }

  @Override
  public void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider) {
    project.getDependencies().add(configurationName, notationProvider);
  }
}
