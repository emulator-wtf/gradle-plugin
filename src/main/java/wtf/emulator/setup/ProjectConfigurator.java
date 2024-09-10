package wtf.emulator.setup;

import com.vdurmont.semver4j.Semver;
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
import wtf.emulator.EwExtension;
import wtf.emulator.EwProperties;
import wtf.emulator.GradleCompat;
import wtf.emulator.attributes.EwArtifactType;
import wtf.emulator.attributes.EwUsage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class ProjectConfigurator {
  private static final String MAVEN_URL = "https://maven.emulator.wtf/releases/";

  private static final String TOOL_CONFIGURATION = "emulatorWtfCli";
  private static final String RESULTS_CONFIGURATION = "emulatorWtf";

  private final Project target;
  private final EwExtension ext;
  private final GradleCompat compat;

  public ProjectConfigurator(Project target, EwExtension ext, GradleCompat compat) {
    this.target = target;
    this.ext = ext;
    this.compat = compat;
  }

  public void configure() {
    setupExtensionDefaults();
    configureRepository();

    Configuration toolConfig = createToolConfiguration();
    Configuration resultsConfig = createResultsConfiguration();

    TaskConfigurator taskConfigurator = new TaskConfigurator(target, ext, toolConfig, resultsConfig);
    VariantConfigurator variantConfigurator = new VariantConfigurator(target, taskConfigurator);

    taskConfigurator.configureRootTask();
    variantConfigurator.configureVariants();
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
}
