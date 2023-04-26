package wtf.emulator;

import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.invocation.Gradle;

public class GradleCompat_6_6 implements GradleCompat {
  private final Gradle gradle;

  public GradleCompat_6_6(Gradle gradle) {
    this.gradle = gradle;
  }

  @Override
  public boolean isConfigurationCacheEnabled() {
    return ((StartParameterInternal) gradle.getStartParameter()).isConfigurationCache();
  }
}
