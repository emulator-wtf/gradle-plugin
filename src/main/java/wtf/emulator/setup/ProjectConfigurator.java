package wtf.emulator.setup;

import com.vdurmont.semver4j.Semver;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.initialization.resolve.RepositoriesMode;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskProvider;
import wtf.emulator.EwExecSummaryTask;
import wtf.emulator.EwExecTask;
import wtf.emulator.EwExtension;
import wtf.emulator.EwProperties;
import wtf.emulator.GradleCompat;
import wtf.emulator.PrintMode;
import wtf.emulator.async.CollectResultsTask;
import wtf.emulator.async.EwAsyncExecService;
import wtf.emulator.attributes.EwArtifactType;
import wtf.emulator.attributes.EwUsage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectConfigurator {
  private static final String MAVEN_URL = "https://maven.emulator.wtf/releases/";

  private static final String TOOL_CONFIGURATION = "emulatorWtfCli";
  private static final String RESULTS_CONFIGURATION = "emulatorWtf";

  private static final String COLLECT_TASK_NAME = "collectEmulatorWtfResults";
  private static final String ROOT_TASK_NAME = "testWithEmulatorWtf";

  private final Project target;
  private final EwExtension ext;
  private final GradleCompat compat;

  public ProjectConfigurator(Project target, EwExtension ext, GradleCompat compat) {
    this.target = target;
    this.ext = ext;
    this.compat = compat;
  }

  public void configure() {
    Provider<EwAsyncExecService> asyncService = registerAsyncService();
    setupExtensionDefaults();
    configureRepository();

    Configuration toolConfig = createToolConfiguration();
    Configuration resultsConfig = createResultsConfiguration();

    createCollectResultsTask(toolConfig, asyncService);

    TaskProvider<EwExecSummaryTask> rootTask = createRootTask(resultsConfig);

    SetProperty<String> failureCollector = target.getObjects().setProperty(String.class);

    addFailurePrinter(failureCollector);

    TaskConfigurator taskConfigurator = new TaskConfigurator(target, ext, toolConfig, resultsConfig, rootTask, failureCollector, asyncService);

    VariantConfigurator variantConfigurator = new VariantConfigurator(target, taskConfigurator);

    variantConfigurator.configureVariants();
  }

  private Provider<EwAsyncExecService> registerAsyncService() {
    return target.getGradle().getSharedServices()
        .registerIfAbsent(EwAsyncExecService.NAME, EwAsyncExecService.class, spec -> {});
  }

  private void setupExtensionDefaults() {
    ext.getBaseOutputDir().convention(target.getLayout().getBuildDirectory().dir("test-results"));
    ext.getRepositoryCheckEnabled().convention(true);
  }

  private void configureRepository() {
    if (!EwProperties.ADD_REPOSITORY.getFlag(target, true)) {
      return;
    }

    Semver gradleVersion = new Semver(target.getGradle().getGradleVersion(), Semver.SemverType.LOOSE);
    if (gradleVersion.isGreaterThanOrEqualTo(new Semver("6.8", Semver.SemverType.LOOSE))) {
      // TODO(madis) yuck
      // https://github.com/gradle/gradle/issues/17295
      Settings settings = ((GradleInternal) target.getGradle()).getSettings();

      DependencyResolutionManagement mgmt = settings.getDependencyResolutionManagement();
      boolean registered = mgmt.getRepositories().stream()
          .filter(artifactRepository -> artifactRepository instanceof MavenArtifactRepository)
          .map(artifactRepository -> (MavenArtifactRepository)artifactRepository)
          .anyMatch(ProjectConfigurator::isEmulatorWtfRepo);

      if (registered) {
        return;
      }

      RepositoriesMode mode = settings.getDependencyResolutionManagement().getRepositoriesMode().getOrNull();
      int settingsRepoCount = settings.getDependencyResolutionManagement().getRepositories().size();
      if ((mode == null || mode == RepositoriesMode.PREFER_PROJECT) && settingsRepoCount == 0) {
        registerMavenRepo();
      } else {
        // ping user after project evaluate to allow suppressing this check in dsl
        target.afterEvaluate(evaluated -> {
          if (Boolean.TRUE.equals(ext.getRepositoryCheckEnabled().getOrNull())) {
            throw new GradleException("Missing maven.emulator.wtf repository\n\n" +
                "Either add the following to your dependencyResolutionManagement dependencies block or\n" +
                "suppress this message via emulatorWtf { repositoryCheckEnabled.set(false) }:\n\n" +
                "dependencyResolutionManagement {\n" +
                "  repositories {\n" +
                "    maven(url = \"https://maven.emulator.wtf/releases/\") {\n" +
                "      content { includeGroup(\"wtf.emulator\") }\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
          }
        });
      }
    } else {
      registerMavenRepo();
    }
  }

  private void registerMavenRepo() {
    target.getRepositories().maven(repo -> {
      try {
        repo.setUrl(new URI(MAVEN_URL).toURL());
        repo.mavenContent((desc) -> desc.includeGroup("wtf.emulator"));
      } catch (MalformedURLException | URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  private static boolean isEmulatorWtfRepo(MavenArtifactRepository it) {
    return MAVEN_URL.equals(it.getUrl().toString()) || MAVEN_URL.equals(it.getUrl() + "/");
  }

  private Configuration createToolConfiguration() {
    final Configuration toolConfig = target.getConfigurations().maybeCreate(TOOL_CONFIGURATION);
    compat.addProviderDependency(target, TOOL_CONFIGURATION, ext.getVersion().map(version -> "wtf.emulator:ew-cli:" + version));
    return toolConfig;
  }

  private Configuration createResultsConfiguration() {
    final Configuration resultsConfig = target.getConfigurations().maybeCreate(RESULTS_CONFIGURATION);
    resultsConfig.setCanBeConsumed(true);
    resultsConfig.setCanBeResolved(true);

    resultsConfig.attributes(attributes -> {
      attributes.attribute(Category.CATEGORY_ATTRIBUTE, target.getObjects().named(Category.class, Category.VERIFICATION));
      attributes.attribute(Usage.USAGE_ATTRIBUTE, target.getObjects().named(EwUsage.class, EwUsage.EW_USAGE));
      attributes.attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
    });

    return resultsConfig;
  }

  private void createCollectResultsTask(Configuration toolConfig, Provider<EwAsyncExecService> service) {
    // if asked, create collect results task
    target.afterEvaluate(_proj -> {
      if (ext.getCollectResultsTaskEnabled().getOrElse(false)) {
        TaskProvider<CollectResultsTask> collectTask = target.getTasks().register(COLLECT_TASK_NAME, CollectResultsTask.class, task -> {
          target.getRootProject().subprojects(sub -> sub.getTasks().matching(t -> EwExecTask.class.isAssignableFrom(t.getClass())).forEach(task::mustRunAfter));

          task.setDescription("Collect emulator.wtf test results from all modules (async only)");

          task.getClasspath().set(toolConfig);
          task.getExecService().set(service);

          task.getOutputsDir().set(ext.getBaseOutputDir());
          task.getOutputTypes().set(ext.getOutputs());
          task.getPrintOutput().set(ext.getPrintOutput());

          task.getProxyHost().set(ext.getProxyHost());
          task.getProxyPort().set(ext.getProxyPort());
          task.getProxyUser().set(ext.getProxyUser());
          task.getProxyPassword().set(ext.getProxyPassword());

          // collect results task is never up-to-date
          task.getOutputs().upToDateWhen(it -> false);
        });
      }
    });
  }

  private TaskProvider<EwExecSummaryTask> createRootTask(Configuration resultsConfiguration) {
    // create root anchor task
    return target.getTasks().register(ROOT_TASK_NAME, EwExecSummaryTask.class, task -> {
      task.setDescription("Run instrumentation tests of all variants with emulator.wtf");
      task.getPrintMode().set(PrintMode.ALL);
      // root task depends on result summary files
      task.dependsOn(resultsConfiguration);
      task.getInputSummaryFiles().set(resultsConfiguration.getOutgoing().getArtifacts().getFiles().plus(
          resultsConfiguration.getIncoming().artifactView((view) -> {
            view.getAttributes().attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
          }).getFiles()
      ));
      // root task is never up-to-date
      task.getOutputs().upToDateWhen(it -> false);
    });
  }

  private void addFailurePrinter(SetProperty<String> failureCollector) {
    boolean configCache = compat.isConfigurationCacheEnabled();
    if (!configCache) {
      target.getGradle().addBuildListener(new BuildAdapter() {
        @Override
        @SuppressWarnings("deprecation")
        public void buildFinished(BuildResult result) {
          if (ext.getIgnoreFailures().getOrElse(false)) {
            List<String> failures = failureCollector.get().stream().filter(it -> it != null && !it.isEmpty()).collect(Collectors.toList());
            if (!failures.isEmpty()) {
              target.getLogger().error("");
              failures.forEach(target.getLogger()::error);
            }
          }
        }
      });
    }
  }
}
