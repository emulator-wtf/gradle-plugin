package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Category;
import org.gradle.api.model.ObjectFactory;
import wtf.emulator.junit.JUnitResults;

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

  @Override
  public Attribute<String> getArtifactTypeAttribute() {
    return ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE;
  }

  @Override
  public void reportTestResults(ObjectFactory objects, JUnitResults junitResults, @Nullable String resultsUrl) {
    /* intentionally empty */
  }

}
