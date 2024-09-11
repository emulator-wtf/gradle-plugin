package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.initialization.resolve.RepositoriesMode;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public class GradleCompat_7_0 implements GradleCompat {
  protected final Gradle gradle;

  public GradleCompat_7_0(Gradle gradle) {
    this.gradle = gradle;
  }

  @Override
  public void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider) {
    project.getDependencies().add(configurationName, notationProvider);
  }

  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
    //noinspection UnstableApiUsage
    return configureGradlePropertyProvider(project.getProviders().gradleProperty(name)).getOrNull();
  }

  @Override
  public String getCategoryAttributeVerification() {
    return "verification";
  }

  @Override
  public boolean canAddMavenRepoToProject(Project project) {
    Settings settings = getGradleSettings(project);

    RepositoriesMode mode = settings.getDependencyResolutionManagement().getRepositoriesMode().getOrNull();
    int settingsRepoCount = settings.getDependencyResolutionManagement().getRepositories().size();

    return (mode == null || mode == RepositoriesMode.PREFER_PROJECT) && settingsRepoCount == 0;
  }

  @Override
  public boolean isRepoRegistered(Project project, String repoUrl) {
    DependencyResolutionManagement mgmt = getGradleSettings(project).getDependencyResolutionManagement();
    return mgmt.getRepositories().stream()
      .filter(artifactRepository -> artifactRepository instanceof MavenArtifactRepository)
      .map(artifactRepository -> (MavenArtifactRepository) artifactRepository)
      .anyMatch(it -> repoUrl.equals(it.getUrl().toString()) || repoUrl.equals(it.getUrl() + "/"));
  }

  private Settings getGradleSettings(Project project) {
    // TODO(madis) yuck
    // https://github.com/gradle/gradle/issues/17295
    return ((GradleInternal) project.getGradle()).getSettings();
  }

  protected Provider<String> configureGradlePropertyProvider(Provider<String> provider) {
    //noinspection UnstableApiUsage
    return provider.forUseAtConfigurationTime();
  }
}
