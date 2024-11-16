package wtf.emulator.setup;

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
import wtf.emulator.EwExtension;
import wtf.emulator.EwExtensionInternal;
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
  private static final String RESULTS_CONFIGURATION = "emulatorwtf";
  private static final String RESULTS_EXPORT_CONFIGURATION = "_emulatorwtf_export";
  private static final String RESULTS_IMPORT_CONFIGURATION = "_emulatorwtf_import";

  private final Project target;
  private final EwExtension ext;
  private final EwExtensionInternal extInternals;
  private final GradleCompat compat;

  public ProjectConfigurator(Project target, EwExtension ext, EwExtensionInternal extInternals, GradleCompat compat) {
    this.target = target;
    this.ext = ext;
    this.extInternals = extInternals;
    this.compat = compat;
  }

  public void configure() {
    setupExtensionDefaults();
    configureRepository();

    Provider<Configuration> toolConfig = createToolConfiguration();
    Configuration resultsExportConfig = createResultsExportConfiguration();
    Provider<Configuration> resultsImportConfig = createResultsImportConfiguration(resultsExportConfig);

    TaskConfigurator taskConfigurator = new TaskConfigurator(target, ext, extInternals, toolConfig, resultsExportConfig, resultsImportConfig);
    VariantConfigurator variantConfigurator = new VariantConfigurator(target, compat, taskConfigurator);

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

    if (!isRepoRegistered(MAVEN_URL)) {
      if (canAddMavenRepoToProject()) {
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

  private Provider<Configuration> createToolConfiguration() {
    Provider<Configuration> toolConfig = target.getConfigurations().register(TOOL_CONFIGURATION, config -> {
      config.setVisible(false);
      config.setCanBeConsumed(false);
      config.setCanBeResolved(true);
      target.getDependencies().add(TOOL_CONFIGURATION, ext.getVersion().map(version -> "wtf.emulator:ew-cli:" + version));
    });
    return toolConfig;
  }

  private Configuration createResultsExportConfiguration() {
    final Configuration resultsExportConfig = target.getConfigurations().maybeCreate(RESULTS_EXPORT_CONFIGURATION);
    resultsExportConfig.setCanBeConsumed(true);
    resultsExportConfig.setCanBeResolved(true);
    resultsExportConfig.setVisible(false);

    resultsExportConfig.attributes(attributes -> {
      attributes.attribute(Category.CATEGORY_ATTRIBUTE, target.getObjects().named(Category.class, compat.getCategoryAttributeVerification()));
      attributes.attribute(Usage.USAGE_ATTRIBUTE, target.getObjects().named(EwUsage.class, EwUsage.EW_USAGE));
      attributes.attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
    });

    return resultsExportConfig;
  }

  private Provider<Configuration> createResultsImportConfiguration(Configuration resultsExportConfig) {
    final Configuration resultsConfig = target.getConfigurations().maybeCreate(RESULTS_CONFIGURATION);
    resultsConfig.setCanBeConsumed(false);
    resultsConfig.setCanBeResolved(false);
    resultsConfig.setVisible(true);

    return target.getConfigurations().register(RESULTS_IMPORT_CONFIGURATION, config -> {
      config.setCanBeConsumed(false);
      config.setCanBeResolved(true);
      config.setVisible(false);
      config.extendsFrom(resultsConfig);
      config.extendsFrom(resultsExportConfig); // local loopback of artifacts

      config.attributes(attributes -> {
        attributes.attribute(Category.CATEGORY_ATTRIBUTE, target.getObjects().named(Category.class, compat.getCategoryAttributeVerification()));
        attributes.attribute(Usage.USAGE_ATTRIBUTE, target.getObjects().named(EwUsage.class, EwUsage.EW_USAGE));
        attributes.attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
      });
    });
  }

  private boolean canAddMavenRepoToProject() {
    Settings settings = getGradleSettings();

    RepositoriesMode mode = settings.getDependencyResolutionManagement().getRepositoriesMode().getOrNull();
    int settingsRepoCount = settings.getDependencyResolutionManagement().getRepositories().size();

    return (mode == null || mode == RepositoriesMode.PREFER_PROJECT) && settingsRepoCount == 0;
  }

  public boolean isRepoRegistered(String repoUrl) {
    DependencyResolutionManagement mgmt = getGradleSettings().getDependencyResolutionManagement();
    return mgmt.getRepositories().stream()
      .filter(artifactRepository -> artifactRepository instanceof MavenArtifactRepository)
      .map(artifactRepository -> (MavenArtifactRepository) artifactRepository)
      .anyMatch(it -> repoUrl.equals(it.getUrl().toString()) || repoUrl.equals(it.getUrl() + "/"));
  }

  private Settings getGradleSettings() {
    // TODO(madis) yuck
    // https://github.com/gradle/gradle/issues/17295
    return ((GradleInternal) target.getGradle()).getSettings();
  }
}
