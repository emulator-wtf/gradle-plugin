package wtf.emulator;

import com.vdurmont.semver4j.Semver;

public class GradleCompatFactory {
  private static final Semver GRADLE_8_13 = new Semver("8.13", Semver.SemverType.LOOSE);
  private static final Semver GRADLE_9_4_0 = new Semver("9.4.0", Semver.SemverType.LOOSE);

  public static GradleCompat get(String gradleVersion) {
    Semver version = new Semver(gradleVersion, Semver.SemverType.LOOSE);

    if (version.isGreaterThanOrEqualTo(GRADLE_9_4_0)) {
      return new GradleCompat_9_4();
    } else if (version.isGreaterThanOrEqualTo(GRADLE_8_13)) {
      return new GradleCompat_8_13();
    } else {
      return new GradleCompat_8_0();
    }
  }
}
