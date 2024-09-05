package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_7_6 extends GradleCompat_7_4 {
  public GradleCompat_7_6(Gradle gradle) {
    super(gradle);
  }

  @Override
  public boolean isConfigurationCacheEnabled() {
    return gradle.getStartParameter().isConfigurationCacheRequested();
  }
}
