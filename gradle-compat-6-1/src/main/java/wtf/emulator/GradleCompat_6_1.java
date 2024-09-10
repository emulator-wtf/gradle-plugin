package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public class GradleCompat_6_1 implements GradleCompat {

  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
    Object value = project.getRootProject().findProperty(name);
    if (value == null) {
      return null;
    }
    return value.toString();
  }

  @Override
  public void addProviderDependency(Project project, String configurationName, Provider<String> notationProvider) {
    // old Gradle versions don't support Provider<String>, loop through afterEvaluate instead :(
    project.afterEvaluate(it ->
      it.getDependencies().add(configurationName, notationProvider.get())
    );
  }

  @Override
  public boolean canAddMavenRepoToProject(Project project) {
    return true;
  }

  @Override
  public boolean isRepoRegistered(Project project, String repository) {
    return false;
  }

  @Override
  public String getCategoryAttributeVerification() {
    return "verification";
  }
}
