package wtf.emulator;

import com.vdurmont.semver4j.Semver;
import org.gradle.api.invocation.Gradle;

public class GradleCompatFactory {
  private static final Semver GRADLE_6_6 = new Semver("6.6",Semver.SemverType.LOOSE);
  private static final Semver GRADLE_7_6 = new Semver("7.6",Semver.SemverType.LOOSE);

  public static GradleCompat get(Gradle gradle) {
    Semver gradleVersion = new Semver(gradle.getGradleVersion(), Semver.SemverType.LOOSE);

    if (gradleVersion.isGreaterThanOrEqualTo(GRADLE_7_6)) {
      return new GradleCompat_7_6(gradle);
    } else if (gradleVersion.isGreaterThanOrEqualTo(GRADLE_6_6)) {
      return new GradleCompat_6_6(gradle);
    } else {
      return new GradleCompat_6_1();
    }
  }
}
