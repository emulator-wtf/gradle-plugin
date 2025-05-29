package wtf.emulator.setup;

import com.android.build.VariantOutput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.TestExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.attributes.VariantAttr;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import wtf.emulator.GradleCompat;

import java.util.Optional;

import static com.android.build.VariantOutput.FilterType.ABI;

public class VariantConfigurator {
  private final Project target;
  private final GradleCompat compat;
  private final TaskConfigurator taskConfigurator;

  public VariantConfigurator(Project target, GradleCompat compat, TaskConfigurator taskConfigurator) {
    this.target = target;
    this.compat = compat;
    this.taskConfigurator = taskConfigurator;
  }

  public void configureVariants() {

    // configure application builds
    target.getPluginManager().withPlugin("com.android.application", plugin -> {
      AppExtension android = target.getExtensions().getByType(AppExtension.class);
      android.getApplicationVariants().configureEach(variant -> configureAppVariant(android, variant));
    });

    // configure library builds
    target.getPluginManager().withPlugin("com.android.library", plugin -> {
      LibraryExtension android = target.getExtensions().getByType(LibraryExtension.class);
      android.getLibraryVariants().configureEach(variant -> configureLibraryVariant(android, variant));
    });

    // configure test project builds
    target.getPluginManager().withPlugin("com.android.test", plugin -> {
      TestExtension android = target.getExtensions().getByType(TestExtension.class);
      android.getApplicationVariants().configureEach(variant -> configureTestVariant(android, variant));
    });

    //TODO(madis) configure feature builds
  }

  private void configureAppVariant(BaseExtension android, ApplicationVariant variant) {
    TestVariant testVariant = variant.getTestVariant();
    if (testVariant != null) {
      taskConfigurator.configureEwTask(android, variant, task -> {
        // TODO(madis) we could do better than main here, technically we do know the list of
        //             devices we're going to run against..
        BaseVariantOutput appOutput = getVariantOutput(testVariant.getTestedVariant());
        BaseVariantOutput testOutput = getVariantOutput(testVariant);

        task.dependsOn(testVariant.getPackageApplicationProvider());
        task.dependsOn(variant.getPackageApplicationProvider());

        task.getApks().set(target.files(appOutput.getOutputFile()));

        task.getTestApk().set(testOutput.getOutputFile());
      });
    }
  }

  private void configureLibraryVariant(BaseExtension android, LibraryVariant variant) {
    TestVariant testVariant = variant.getTestVariant();
    if (testVariant != null) {
      taskConfigurator.configureEwTask(android, variant, task -> {
        // library projects only have the test apk
        BaseVariantOutput testOutput = getVariantOutput(testVariant);
        task.dependsOn(testVariant.getPackageApplicationProvider());
        task.getLibraryTestApk().set(testOutput.getOutputFile());
      });
    }
  }

  private void configureTestVariant(TestExtension android, ApplicationVariant variant) {
    taskConfigurator.configureEwTask(android, variant, task -> {
      // test projects have the test apk as a main output
      BaseVariantOutput testOutput = getVariantOutput(variant);
      task.dependsOn(variant.getPackageApplicationProvider());
      task.getTestApk().set(testOutput.getOutputFile());

      // look up the referenced target variant
      // TODO(madis) use artifact apis here instead?
      final String variantName = variant.getName();
      Provider<Configuration> testedApks = target.getConfigurations().named(variantName + "TestedApks");
      task.getApks().set(
        testedApks.map(it ->
          it.getIncoming().artifactView(view -> {
            view.getAttributes().attribute(VariantAttr.ATTRIBUTE,
              target.getObjects().named(VariantAttr.class, variantName));
            view.getAttributes().attribute(compat.getArtifactTypeAttribute(), "apk");
          }).getFiles()
        )
      );
    });
  }

  private static BaseVariantOutput getVariantOutput(BaseVariant variant) {
    // if there are splits, prefer x86 split as they're faster to upload
    Optional<BaseVariantOutput> x86Output = variant.getOutputs().stream()
        .filter(it -> it.getOutputType().equals(VariantOutput.FULL_SPLIT))
        .filter(it -> it.getFilterTypes().size() == 1 && it.getFilterTypes().contains(ABI.name()))
        .filter(it -> it.getFilters().stream().anyMatch(filter -> filter.getFilterType().equals(ABI.name()) && filter.getIdentifier().equals("x86")))
        .findFirst();

    Optional<BaseVariantOutput> universalSplit = variant.getOutputs().stream()
        .filter(it -> it.getOutputType().equals(VariantOutput.FULL_SPLIT))
        .filter(it -> it.getFilterTypes().isEmpty())
        .findFirst();

    Optional<BaseVariantOutput> mainOutput = variant.getOutputs().stream()
        .filter(it -> it.getOutputType().equals(VariantOutput.MAIN))
        .findFirst();

    return x86Output
        .or(() -> universalSplit)
        .or(() -> mainOutput)
        .orElseThrow(() -> new IllegalStateException("Variant " + variant.getName() + " has no x86 outputs!"));
  }
}
