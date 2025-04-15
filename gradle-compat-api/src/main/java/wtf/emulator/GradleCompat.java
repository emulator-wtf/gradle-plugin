package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.model.ObjectFactory;
import wtf.emulator.junit.JUnitResults;

import javax.annotation.Nullable;

public interface GradleCompat {
  @Nullable
  String getGradleProperty(Project project, String name);

  Attribute<String> getArtifactTypeAttribute();
  
  String getCategoryAttributeVerification();

  void reportTestResults(ObjectFactory objects, JUnitResults junitResults, @Nullable String resultsUrl);
}
