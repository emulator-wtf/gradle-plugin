package wtf.emulator;

import com.android.build.api.variant.TestVariant;
import com.android.build.api.variant.Variant;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

import static wtf.emulator.AgpCompatUtils.getAndroidTest;

public class AgpCompat_8_5 implements AgpCompat {
  @Override
  public AgpVariantDataHolder collectAgpVariantData(Project target) {
    return new Agp85DataHolder();
  }

  public void populateAgpVariantData(AgpVariantDataHolder holder, Variant variant, AgpVariantData out) {
    if (!(holder instanceof Agp85DataHolder)) {
      return;
    }

    final var androidTest = getAndroidTest(variant);
    if (androidTest != null) {
      out.getTestCoverageEnabled().set(androidTest.getCodeCoverageEnabled());
      out.getInstrumentationRunnerArguments().set(androidTest.getInstrumentationRunnerArguments());
    } else if (variant instanceof TestVariant) {
      // TODO(madis) no testCoverageEnabled on TestVariant?
      out.getInstrumentationRunnerArguments().set(((TestVariant) variant).getInstrumentationRunnerArguments());
    }
  }

  @Override
  @Nullable
  public Provider<FileCollection> getTestedApks(Project project, TestVariant testVariant) {
    return null;
  }

  @Override
  @Nullable
  public Provider<Directory> getTestedApkDirectory(Project project, TestVariant testVariant) {
    return testVariant.getTestedApks();
  }
}
