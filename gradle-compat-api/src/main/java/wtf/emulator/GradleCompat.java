package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public interface GradleCompat {
  @Nullable
  String getGradleProperty(Project project, String name);

  Attribute<String> getArtifactTypeAttribute();
  
  String getCategoryAttributeVerification();
}
