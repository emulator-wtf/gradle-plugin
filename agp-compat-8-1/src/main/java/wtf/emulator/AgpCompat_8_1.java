package wtf.emulator;

import com.android.build.api.variant.TestVariant;
import com.android.build.api.variant.Variant;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.TestExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.attributes.VariantAttr;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class AgpCompat_8_1 implements AgpCompat {
  @Override
  public AgpVariantDataHolder collectAgpVariantData(Project target) {
    final var data = new HashMap<String, BaseVariant>();
    final var extensionRef = new AtomicReference<BaseExtension>(null);

    target.getPluginManager().withPlugin("com.android.application", plugin -> {
      AppExtension android = target.getExtensions().getByType(AppExtension.class);
      extensionRef.set(android);
      android.getApplicationVariants().configureEach(it -> data.put(it.getName(), it));
    });

    target.getPluginManager().withPlugin("com.android.library", plugin -> {
      LibraryExtension android = target.getExtensions().getByType(LibraryExtension.class);
      extensionRef.set(android);
      android.getLibraryVariants().configureEach(it -> data.put(it.getName(), it));
    });

    target.getPluginManager().withPlugin("com.android.test", plugin -> {
      TestExtension android = target.getExtensions().getByType(TestExtension.class);
      extensionRef.set(android);
      android.getApplicationVariants().configureEach(it -> data.put(it.getName(), it));
    });

    return new Agp81DataHolder(extensionRef, data);
  }

  @Override
  @Nullable
  public Provider<FileCollection> getTestedApks(Project project, TestVariant testVariant) {
    final String variantName = testVariant.getName();
    Provider<Configuration> testedApks = project.getConfigurations().named(variantName + "TestedApks");

    return testedApks.map(config -> config.getIncoming().artifactView(view -> {
      view.getAttributes().attribute(VariantAttr.ATTRIBUTE, project.getObjects().named(VariantAttr.class, variantName));
      view.getAttributes().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "apk");
    }).getFiles());
  }

  @Override
  @Nullable
  public Provider<Directory> getTestedApkDirectory(Project project, TestVariant testVariant) {
    return null;
  }

  @Override
  public void populateAgpVariantData(AgpVariantDataHolder holder, Variant variant, AgpVariantData out) {
    if (!(holder instanceof Agp81DataHolder)) {
      return;
    }

    final var legacyVariant = ((Agp81DataHolder) holder).data().get(variant.getName());
    if (legacyVariant != null) {
      out.getInstrumentationRunnerArguments().set(legacyVariant.getMergedFlavor().getTestInstrumentationRunnerArguments());
      out.getTestCoverageEnabled().set(legacyVariant.getBuildType().isTestCoverageEnabled());
    }
  }
}
