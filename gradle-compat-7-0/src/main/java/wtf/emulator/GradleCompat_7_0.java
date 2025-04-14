package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import wtf.emulator.junit.JUnitResults;

import javax.annotation.Nullable;

public class GradleCompat_7_0 implements GradleCompat {
  Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);

  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
    return project.getProviders().gradleProperty(name).forUseAtConfigurationTime().getOrNull();
  }

  @Override
  public String getCategoryAttributeVerification() {
    return "verification";
  }

  @Override
  public Attribute<String> getArtifactTypeAttribute() {
    return ARTIFACT_TYPE_ATTRIBUTE;
  }

  @Override
  public void reportTestResults(Project project, JUnitResults junitResults, @Nullable String resultsUrl) {
    /* intentionally empty */
  }
}
