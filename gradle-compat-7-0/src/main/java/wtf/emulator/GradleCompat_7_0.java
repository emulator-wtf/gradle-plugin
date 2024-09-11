package wtf.emulator;

import org.gradle.api.Project;

import javax.annotation.Nullable;

public class GradleCompat_7_0 implements GradleCompat {
  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
    return project.getProviders().gradleProperty(name).forUseAtConfigurationTime().getOrNull();
  }

  @Override
  public String getCategoryAttributeVerification() {
    return "verification";
  }
}
