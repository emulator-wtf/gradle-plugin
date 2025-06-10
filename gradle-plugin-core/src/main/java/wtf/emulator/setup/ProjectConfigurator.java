package wtf.emulator.setup;

import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.dsl.Device;
import com.android.build.api.dsl.ManagedDevices;
import com.android.build.api.instrumentation.manageddevice.DeviceDslRegistration;
import com.android.build.api.variant.AndroidComponentsExtension;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.api.initialization.resolve.RepositoriesMode;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;
import wtf.emulator.EwExtension;
import wtf.emulator.EwExtensionInternal;
import wtf.emulator.EwProperties;
import wtf.emulator.GradleCompat;
import wtf.emulator.attributes.EwArtifactType;
import wtf.emulator.attributes.EwUsage;
import wtf.emulator.gmd.EwDeviceSetupConfigureAction;
import wtf.emulator.gmd.EwDeviceSetupTaskAction;
import wtf.emulator.gmd.EwDeviceTestRunConfigureAction;
import wtf.emulator.gmd.EwDeviceTestRunTaskAction;
import wtf.emulator.gmd.EwManagedDevice;
import wtf.emulator.gmd.EwManagedDeviceFactory;
import wtf.emulator.gmd.EwManagedDeviceImpl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class ProjectConfigurator {
  private static final String MAVEN_URL = "https://maven.emulator.wtf/releases/";

  public static final String TOOL_CONFIGURATION = "emulatorWtfCli";
  private static final String RESULTS_CONFIGURATION = "emulatorwtf";
  private static final String RESULTS_EXPORT_CONFIGURATION = "_emulatorwtf_export";
  private static final String RESULTS_IMPORT_CONFIGURATION = "_emulatorwtf_import";

  private static final String EW_DEVICES_CONTAINER_KEY = "ewDevicesContainer";

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

    registerGmdDeviceType();
    registerManagedDeviceExtension();
  }

  private void registerGmdDeviceType() {
    target.getExtensions().getByType(AndroidComponentsExtension.class)
      .getManagedDeviceRegistry()
      .registerDeviceType(EwManagedDevice.class,
        (Function1<? super DeviceDslRegistration<EwManagedDevice>, Unit>) registration -> {
          registration.setDslImplementationClass(EwManagedDeviceImpl.class);
          registration.setSetupActions(EwDeviceSetupConfigureAction.class, EwDeviceSetupTaskAction.class);
          registration.setTestRunActions(EwDeviceTestRunConfigureAction.class, EwDeviceTestRunTaskAction.class);
          return Unit.INSTANCE;
        });
  }

  private void registerManagedDeviceExtension() {
    CommonExtension<?, ?, ?, ?, ?> androidCommonExtension = target.getExtensions().findByType(CommonExtension.class);

    if (androidCommonExtension == null) {
      target.getLogger().debug("Android common extension not found. Skipping ewDevices setup for project {}", target.getPath());
      return;
    }

    ManagedDevices managedDevices = androidCommonExtension.getTestOptions().getManagedDevices();
    ObjectFactory objects = target.getObjects();

    ExtensionAware extensionAwareManagedDevices = (ExtensionAware) managedDevices;
    ExtraPropertiesExtension extraProperties = extensionAwareManagedDevices.getExtensions().getExtraProperties();

    if (extraProperties.has(EW_DEVICES_CONTAINER_KEY)) {
      return;
    }

    EwManagedDeviceFactory ewDeviceFactory = objects.newInstance(EwManagedDeviceFactory.class);

    NamedDomainObjectContainer<EwManagedDevice> ewDevicesContainer =
      objects.domainObjectContainer(EwManagedDevice.class, ewDeviceFactory);
    extraProperties.set(EW_DEVICES_CONTAINER_KEY, ewDevicesContainer);

    // When an EwManagedDevice is added to our ewDevices container, add it to allDevices
    ewDevicesContainer.whenObjectAdded(new Action<EwManagedDevice>() {
      @Override
      public void execute(@NotNull EwManagedDevice ewDevice) {
        if (managedDevices.getAllDevices().findByName(ewDevice.getName()) == null ||
          !(managedDevices.getAllDevices().findByName(ewDevice.getName()) instanceof EwManagedDevice)) {
          managedDevices.getAllDevices().add(ewDevice);
        }
      }
    });
    // When an EwManagedDevice is removed from our ewDevices container, remove it from allDevices
    ewDevicesContainer.whenObjectRemoved(new Action<EwManagedDevice>() {
      @Override
      public void execute(@NotNull EwManagedDevice ewDevice) {
        Device deviceInAll = managedDevices.getAllDevices().findByName(ewDevice.getName());
        if (deviceInAll instanceof EwManagedDevice) { // Make sure it's the correct type
          managedDevices.getAllDevices().remove(deviceInAll);
        }
      }
    });

    // When any Device is added to allDevices, if it's an EwManagedDevice, add it to our ewDevices container
    managedDevices.getAllDevices().whenObjectAdded(device -> {
      if (device instanceof EwManagedDevice) {
        EwManagedDevice ewDevice = (EwManagedDevice) device;
        if (ewDevicesContainer.findByName(ewDevice.getName()) == null) {
          ewDevicesContainer.add(ewDevice);
        }
      }
    });
    // When any Device is removed from allDevices, if it's an EwManagedDevice, remove it from our ewDevices container
    managedDevices.getAllDevices().whenObjectRemoved(device -> {
      if (device instanceof EwManagedDevice) {
        EwManagedDevice ewDevice = (EwManagedDevice) device;
        EwManagedDevice deviceInEw = ewDevicesContainer.findByName(ewDevice.getName());
        if (deviceInEw != null) {
          ewDevicesContainer.remove(deviceInEw);
        }
      }
    });
    target.getLogger().debug("ewDevices container configured for project {}", target.getPath());
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
    // has to be created eagerly, otherwise nothing will run from the summary tasks
    // TODO(madis) find a better workaround for this
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
    // has to be created eagerly, otherwise nothing will run from the summary tasks
    // TODO(madis) find a better workaround for this
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
