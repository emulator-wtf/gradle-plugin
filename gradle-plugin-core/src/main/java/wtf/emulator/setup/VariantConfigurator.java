package wtf.emulator.setup;

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
import org.gradle.api.Action;
import org.gradle.api.Project;
import wtf.emulator.AgpCompat;
import wtf.emulator.AgpCompatFactory;
import wtf.emulator.AgpVariantData;
import wtf.emulator.AgpVariantDataHolder;
import wtf.emulator.DslInternals;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwInvokeDsl;
import wtf.emulator.EwVariantFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static wtf.emulator.setup.StringUtils.capitalize;

public class VariantConfigurator {
  private final Project target;
  private final EwExtension ext;
  private final TaskConfigurator taskConfigurator;

  public VariantConfigurator(Project target, EwExtension ext, TaskConfigurator taskConfigurator) {
    this.target = target;
    this.ext = ext;
    this.taskConfigurator = taskConfigurator;
  }

  public void configureVariants() {
    // configure application builds
    target.getPluginManager().withPlugin("com.android.application", plugin -> {
      ApplicationAndroidComponentsExtension android = target.getExtensions().getByType(ApplicationAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super ApplicationVariant, Unit>) it -> configureAppVariant(compat, holder, it));
    });

    target.getPluginManager().withPlugin("com.android.library", plugin -> {
      LibraryAndroidComponentsExtension android = target.getExtensions().getByType(LibraryAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super LibraryVariant, Unit>) it -> configureLibraryVariant(compat, holder, it));
    });

    target.getPluginManager().withPlugin("com.android.test", plugin -> {
      TestAndroidComponentsExtension android = target.getExtensions().getByType(TestAndroidComponentsExtension.class);
      final var compat = AgpCompatFactory.getAgpCompat(android.getPluginVersion());
      final var holder = compat.collectAgpVariantData(target);
      android.onVariants(android.selector().all(), (Function1<? super TestVariant, Unit>) it -> configureTestVariant(compat, holder, it));
    });

    //TODO(madis) configure feature builds

    //TODO(madis) KMP?
  }

  private Unit configureAppVariant(AgpCompat compat, AgpVariantDataHolder holder, ApplicationVariant variant) {
    AndroidTest testVariant = variant.getAndroidTest();
    if (testVariant != null) {
      final var defaultConfigCtx = VariantConfigurationContext.builder(compat, holder, variant.getName(), ext)
        .app(variant, testVariant)
        .build();
      createEwTasks(defaultConfigCtx, ext);
    }
    return Unit.INSTANCE;
  }

  private Unit configureLibraryVariant(AgpCompat compat, AgpVariantDataHolder holder, LibraryVariant variant) {
    AndroidTest testVariant = variant.getAndroidTest();
    if (testVariant != null) {
      final var defaultConfigCtx = VariantConfigurationContext.builder(compat, holder, variant.getName(), ext)
        .library(variant, testVariant)
        .build();
      createEwTasks(defaultConfigCtx, ext);
    }
    return Unit.INSTANCE;
  }

  private Unit configureTestVariant(AgpCompat compat, AgpVariantDataHolder holder, TestVariant variant) {
    final var defaultConfigCtx = VariantConfigurationContext.builder(compat, holder, variant.getName(), ext)
      .test(target, variant)
      .build();
    createEwTasks(defaultConfigCtx, ext);
    return Unit.INSTANCE;
  }

  @SuppressWarnings("EagerGradleConfiguration") // configs have to be loaded eagerly to register the tasks
  private void createEwTasks(VariantConfigurationContext defaultConfigCtx, EwExtension ext) {
    createEwTask(defaultConfigCtx);
    ext.getConfigurations().all(config -> {
      final var configCtx = defaultConfigCtx.toBuilder()
        .invokeName(config.getName() + capitalize(defaultConfigCtx.invokeName()))
        .dsl(config)
        .build();
      createEwTask(configCtx);
    });
  }

  private void createEwTask(VariantConfigurationContext ctx) {
    if (isEnabled(ctx.variant(), ctx.dsl())) {
      taskConfigurator.configureEwTask(ctx.invokeName(), ctx.dsl(), task -> {
        // realize variant data
        configureAgpDslDefaults(ctx.compat(), ctx.dsl(), ctx.holder(), ctx.variant(), task);

        task.getBuiltArtifactsLoader().set(ctx.variant().getArtifacts().getBuiltArtifactsLoader());

        if (ctx.appApksFolder() != null) {
          task.getAppApksFolder().set(ctx.appApksFolder());
        }
        if (ctx.appApkFiles() != null) {
          task.getAppApks().set(ctx.appApkFiles());
        }
        if (ctx.testApksFolder() != null) {
          task.getTestApksFolder().set(ctx.testApksFolder());
        }
        if (ctx.libraryTestApksFolder() != null) {
          task.getLibraryTestApksFolder().set(ctx.libraryTestApksFolder());
        }
      });
    }
  }

  private void configureAgpDslDefaults(AgpCompat compat, EwInvokeDsl dsl, AgpVariantDataHolder holder, Variant variant, EwExecTask task) {
    final var variantData = target.getObjects().newInstance(AgpVariantData.class);
    compat.populateAgpVariantData(holder, variant, variantData);

    if (!task.getUseOrchestrator().isPresent()) {
      final var commonExt = target.getExtensions().findByType(CommonExtension.class);
      if (commonExt != null) {
        task.getUseOrchestrator().set("ANDROIDX_TEST_ORCHESTRATOR".equalsIgnoreCase(commonExt.getTestOptions().getExecution()));
      }
    }

    if (!task.getWithCoverage().isPresent()) {
      task.getWithCoverage().set(variantData.getTestCoverageEnabled());
    }

    task.getEnvironmentVariables().set(
      dsl.getEnvironmentVariables().zip(variantData.getInstrumentationRunnerArguments(), (entries, defaults) -> {
        // pick defaults from agp dsl instrumentation runner args, then fill with overrides
        final Map<String, String> out = new HashMap<>(defaults);
        entries.forEach((key, value) -> out.put(key, Objects.toString(value)));
        return out;
      })
    );
  }

  private boolean isEnabled(Variant variant, EwInvokeDsl dsl) {
    Action<EwVariantFilter> filter = DslInternals.getFilter(dsl);
    if (filter != null) {
      EwVariantFilter filterSpec = new EwVariantFilter(variant);
      filter.execute(filterSpec);
      return filterSpec.isEnabled();
    }
    return true;
  }
}
