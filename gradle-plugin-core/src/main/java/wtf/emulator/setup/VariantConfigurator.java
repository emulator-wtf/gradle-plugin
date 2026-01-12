package wtf.emulator.setup;

import com.android.build.api.artifact.SingleArtifact;
import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.variant.AndroidTest;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.ApplicationVariant;
import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.android.build.api.variant.LibraryVariant;
import com.android.build.api.variant.TestAndroidComponentsExtension;
import com.android.build.api.variant.TestVariant;
import com.android.build.api.variant.Variant;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;
import org.gradle.api.Action;
import org.gradle.api.Project;
import wtf.emulator.AgpCompat;
import wtf.emulator.AgpCompatFactory;
import wtf.emulator.AgpVariantData;
import wtf.emulator.AgpVariantDataHolder;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwExtensionInternal;
import wtf.emulator.EwVariantFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VariantConfigurator {
  private final Project target;
  private final EwExtension ext;
  private final EwExtensionInternal extInternals;
  private final TaskConfigurator taskConfigurator;

  public VariantConfigurator(Project target, EwExtension ext, EwExtensionInternal extInternals, TaskConfigurator taskConfigurator) {
    this.target = target;
    this.ext = ext;
    this.extInternals = extInternals;
    this.taskConfigurator = taskConfigurator;
  }

  public void configureVariants() {
    // configure application builds
    target.getPluginManager().withPlugin("com.android.application", plugin -> {
      ApplicationAndroidComponentsExtension android = target.getExtensions().getByType(ApplicationAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super ApplicationVariant, Unit>) it -> filter(compat, holder, it, this::configureAppVariant));
    });

    target.getPluginManager().withPlugin("com.android.library", plugin -> {
      LibraryAndroidComponentsExtension android = target.getExtensions().getByType(LibraryAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super LibraryVariant, Unit>) it -> filter(compat, holder, it, this::configureLibraryVariant));
    });

    target.getPluginManager().withPlugin("com.android.test", plugin -> {
      TestAndroidComponentsExtension android = target.getExtensions().getByType(TestAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super TestVariant, Unit>) it -> filter(compat, holder, it, this::configureTestVariant));
    });

    //TODO(madis) configure feature builds

    //TODO(madis) KMP?
  }

  private Unit configureAppVariant(AgpCompat compat, AgpVariantDataHolder holder, ApplicationVariant variant) {
    AndroidTest testVariant = variant.getAndroidTest();
    if (testVariant != null) {
      taskConfigurator.configureEwTask(variant, task -> {
        // realize variant data
        configureAgpDslDefaults(compat, holder, variant, task);

        // TODO(madis) we could do better than main here, technically we do know the list of
        //             devices we're going to run against..
        task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
        task.getAppApksFolder().set(variant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
        task.getTestApksFolder().set(testVariant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
      });
    }
    return Unit.INSTANCE;
  }

  private Unit configureLibraryVariant(AgpCompat compat, AgpVariantDataHolder holder, LibraryVariant variant) {
    AndroidTest testVariant = variant.getAndroidTest();
    if (testVariant != null) {
      taskConfigurator.configureEwTask(variant, task -> {
        // realize variant data
        configureAgpDslDefaults(compat, holder, variant, task);
        // library projects only have the test apk
        task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
        task.getLibraryTestApksFolder().set(testVariant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
      });
    }
    return Unit.INSTANCE;
  }

  private Unit configureTestVariant(AgpCompat compat, AgpVariantDataHolder holder, TestVariant variant) {
    taskConfigurator.configureEwTask(variant, task -> {
      // realize variant data
      configureAgpDslDefaults(compat, holder, variant, task);

      // connect apk
      task.getBuiltArtifactsLoader().set(variant.getArtifacts().getBuiltArtifactsLoader());
      task.getTestApksFolder().set(variant.getArtifacts().get(SingleArtifact.APK.INSTANCE));

      // look up the referenced target apks
      final var targetApkDir = compat.getTestedApkDirectory(target, variant);
      if (targetApkDir != null) {
        task.getAppApksFolder().set(targetApkDir);
      } else {
        final var targetApkCollection = compat.getTestedApks(target, variant);
        if (targetApkCollection == null) {
          throw new IllegalStateException("Could not configure target app apk for test module " + target.getPath());
        }
        task.getAppApks().set(targetApkCollection);
      }
    });
    return Unit.INSTANCE;
  }

  private void configureAgpDslDefaults(AgpCompat compat, AgpVariantDataHolder holder, Variant variant, EwExecTask task) {
    final var variantData = target.getObjects().newInstance(AgpVariantData.class);
    compat.populateAgpVariantData(holder, variant, variantData);

    if (!task.getUseOrchestrator().isPresent()) {
      final var commonExt = target.getExtensions().findByType(CommonExtension.class);
      task.getUseOrchestrator().set("ANDROIDX_TEST_ORCHESTRATOR".equalsIgnoreCase(commonExt.getTestOptions().getExecution()));
    }

    if (!task.getWithCoverage().isPresent()) {
      task.getWithCoverage().set(variantData.getTestCoverageEnabled());
    }

    task.getEnvironmentVariables().set(
      ext.getEnvironmentVariables().zip(variantData.getInstrumentationRunnerArguments(), (entries, defaults) -> {
        // pick defaults from agp dsl instrumentation runner args, then fill with overrides
        final Map<String, String> out = new HashMap<>(defaults);
        entries.forEach((key, value) -> out.put(key, Objects.toString(value)));
        return out;
      })
    );
  }

  private <VariantType extends Variant> Unit filter(AgpCompat agpCompat, AgpVariantDataHolder holder, VariantType variant, Function3<AgpCompat, AgpVariantDataHolder, VariantType, Unit> configure) {
    if (isEnabled(variant)) {
      configure.invoke(agpCompat, holder, variant);
    }
    return Unit.INSTANCE;
  }

  private boolean isEnabled(Variant variant) {
    Action<EwVariantFilter> filter = extInternals.getFilter();
    if (filter != null) {
      EwVariantFilter filterSpec = new EwVariantFilter(variant);
      filter.execute(filterSpec);
      return filterSpec.isEnabled();
    }
    return true;
  }
}
