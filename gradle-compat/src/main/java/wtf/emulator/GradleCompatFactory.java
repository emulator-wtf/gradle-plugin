package wtf.emulator;

import com.vdurmont.semver4j.Semver;
import org.gradle.api.invocation.Gradle;

public class GradleCompatFactory {
  private static final Semver GRADLE_7_4 = new Semver("7.4",Semver.SemverType.LOOSE);

  public static GradleCompat get(Gradle gradle) {
    Semver gradleVersion = new Semver(gradle.getGradleVersion(), Semver.SemverType.LOOSE);

    if (gradleVersion.isGreaterThanOrEqualTo(GRADLE_7_4)) {
      return new GradleCompat_7_4();
    } else {
      return new GradleCompat_7_0();
    }
  }
}
