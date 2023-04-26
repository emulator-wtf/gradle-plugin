package wtf.emulator;

import org.gradle.api.invocation.Gradle;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_7_6 implements GradleCompat {
  private final Gradle gradle;

  public GradleCompat_7_6(Gradle gradle) {
    this.gradle = gradle;
  }

  @Override
  public boolean isConfigurationCacheEnabled() {
    return gradle.getStartParameter().isConfigurationCacheRequested();
  }
}
