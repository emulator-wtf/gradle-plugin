package wtf.emulator;

import com.vdurmont.semver4j.Semver;
import org.gradle.api.invocation.Gradle;

public class GradleCompatFactory {
  private static final Semver GRADLE_7_4 = new Semver("7.4", Semver.SemverType.LOOSE);
  private static final Semver GRADLE_8_13 = new Semver("8.13", Semver.SemverType.LOOSE);

  public static GradleCompat get(Gradle gradle) {
    return get(gradle.getGradleVersion());
  }

  public static GradleCompat get(String gradleVersion) {
    Semver version = new Semver(gradleVersion, Semver.SemverType.LOOSE);

    if (version.isGreaterThanOrEqualTo(GRADLE_8_13)) {
      return new GradleCompat_8_13();
    } else if (version.isGreaterThanOrEqualTo(GRADLE_7_4)) {
      return new GradleCompat_7_4();
    } else {
      return new GradleCompat_7_0();
    }
  }
}
