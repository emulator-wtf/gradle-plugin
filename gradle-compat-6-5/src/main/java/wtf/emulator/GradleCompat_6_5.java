package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public class GradleCompat_6_5 extends GradleCompat_6_1 {
  protected final Gradle gradle;

  public GradleCompat_6_5(Gradle gradle) {
    this.gradle = gradle;
  }

  @Nullable
  @Override
  public String getGradleProperty(Project project, String name) {
      //noinspection UnstableApiUsage
      return configureGradlePropertyProvider(project.getProviders().gradleProperty(name)).getOrNull();
  }

  protected Provider<String> configureGradlePropertyProvider(Provider<String> provider) {
      //noinspection UnstableApiUsage
      return provider.forUseAtConfigurationTime();
  }
}
