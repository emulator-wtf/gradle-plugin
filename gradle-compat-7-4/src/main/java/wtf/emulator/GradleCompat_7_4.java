package wtf.emulator;

import org.gradle.api.attributes.Category;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;

@SuppressWarnings("UnstableApiUsage")
public class GradleCompat_7_4 extends GradleCompat_7_0 {
  public GradleCompat_7_4(Gradle gradle) {
    super(gradle);
  }

  @Override
  protected Provider<String> configureGradlePropertyProvider(Provider<String> provider) {
    // forUseAtConfigurationTime is now deprecated
    return provider;
  }

  @Override
  public String getCategoryAttributeVerification() {
    return Category.VERIFICATION;
  }
}
