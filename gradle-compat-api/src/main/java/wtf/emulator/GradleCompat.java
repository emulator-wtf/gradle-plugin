package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public interface GradleCompat {
  @Nullable
  String getGradleProperty(Project project, String name);

  void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider);

  boolean canAddMavenRepoToProject(Project project);

  boolean isRepoRegistered(Project project, String repository);
  
  String getCategoryAttributeVerification();
}
