package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public interface GradleCompat {
  boolean isConfigurationCacheEnabled();

  void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider);
}
