package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public interface GradleCompat {
  boolean isConfigurationCacheEnabled();

  @Nullable
  String getGradleProperty(Project project, String name);

  void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider);
}
