package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public interface GradleCompat {
  @Nullable
  String getGradleProperty(Project project, String name);
  
  String getCategoryAttributeVerification();
}
