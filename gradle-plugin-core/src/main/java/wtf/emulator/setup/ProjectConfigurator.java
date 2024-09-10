package wtf.emulator.setup;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
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
  private static final String RESULTS_CONFIGURATION = "emulatorWtf";

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

    Configuration toolConfig = createToolConfiguration();
    Configuration resultsConfig = createResultsConfiguration();

    TaskConfigurator taskConfigurator = new TaskConfigurator(target, ext, extInternals, toolConfig, resultsConfig);
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

    if (!compat.isRepoRegistered(target, MAVEN_URL)) {
      if (compat.canAddMavenRepoToProject(target)) {
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
      attributes.attribute(Category.CATEGORY_ATTRIBUTE, target.getObjects().named(Category.class, compat.getCategoryAttributeVerification()));
      attributes.attribute(Usage.USAGE_ATTRIBUTE, target.getObjects().named(EwUsage.class, EwUsage.EW_USAGE));
      attributes.attribute(EwArtifactType.EW_ARTIFACT_TYPE_ATTRIBUTE, target.getObjects().named(EwArtifactType.class, EwArtifactType.SUMMARY_JSON));
    });

    return resultsConfig;
  }
}
