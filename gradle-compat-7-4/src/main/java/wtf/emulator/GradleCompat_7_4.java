package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.attributes.Category;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_7_4 implements GradleCompat {
  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
    return project.getProviders().gradleProperty(name).getOrNull();
  }

  @Override
  public String getCategoryAttributeVerification() {
    return Category.VERIFICATION;
  }
}
