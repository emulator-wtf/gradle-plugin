package wtf.emulator.gmd;

import com.android.build.api.instrumentation.StaticTestData;
import com.android.build.api.instrumentation.manageddevice.DeviceTestRunParameters;
import com.android.build.api.instrumentation.manageddevice.DeviceTestRunTaskAction;
import com.android.build.api.instrumentation.manageddevice.TestRunData;
import com.android.builder.testing.api.DeviceConfigProvider;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import wtf.emulator.DeviceModel;
import wtf.emulator.EwDeviceSpec;
import wtf.emulator.EwJson;
import wtf.emulator.exec.EwCliExecutor;
import wtf.emulator.exec.EwCliOutput;
import wtf.emulator.exec.EwWorkParameters;
import wtf.emulator.gmd.utp.UtpResultGenerator;
import wtf.emulator.setup.ProviderUtils;
import wtf.emulator.utp.TestSuiteResult;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EwDeviceTestRunTaskAction implements DeviceTestRunTaskAction<EwDeviceTestRunInput> {

  @Inject
  public abstract ExecOperations getExecOperations();

  @Inject
  public abstract ObjectFactory getObjectFactory();

  @Inject
  public abstract ProviderFactory getProviderFactory();

  @Override
  public boolean runTests(@NotNull DeviceTestRunParameters<EwDeviceTestRunInput> deviceTestRunParameters) {
    EwCliExecutor cliExecutor = new EwCliExecutor(EwJson.gson, getExecOperations());
    EwCliOutput output = cliExecutor.invokeCli(testParamsToWorkParams(deviceTestRunParameters));

    generateUtpResuls(deviceTestRunParameters);
    convertResultsXml(deviceTestRunParameters);

    return output.isSuccess();
  }

  private void generateUtpResuls(@NotNull DeviceTestRunParameters<EwDeviceTestRunInput> deviceTestRunParameters) {
    File outputsDir = deviceTestRunParameters.getTestRunData().getOutputDirectory().getAsFile();
    if (outputsDir.exists()) {
      try (Stream<Path> files = Files.find(outputsDir.toPath(), Integer.MAX_VALUE,
        (path, basicFileAttributes) ->
          basicFileAttributes.isRegularFile() && path.toString().endsWith(".txt"))) {
        List<Path> txtFiles = files.collect(Collectors.toList());
        TestSuiteResult testSuiteResult = UtpResultGenerator.generateUtpResults(txtFiles);
        File utpResultsFile = new File(outputsDir, "test-result.pb");
        try (FileOutputStream fos = new FileOutputStream(utpResultsFile)) {
          testSuiteResult.writeTo(fos);
        } catch (IOException e) {
          getLogger().warn("Failed to write UTP results to test-results.pb in {}", outputsDir.getAbsolutePath(), e);
        }
      } catch (IOException e) {
        getLogger().warn("Failed to find .txt files in {}", outputsDir.getAbsolutePath(), e);
      }
    }
  }

  @NotNull
  private EwWorkParameters testParamsToWorkParams(@NotNull DeviceTestRunParameters<EwDeviceTestRunInput> deviceTestRunParameters) {
    EwWorkParameters workParams = getObjectFactory().newInstance(EwWorkParameters.class);
    EwDeviceTestRunInput testRunInput = deviceTestRunParameters.getDeviceInput();
    TestRunData testRunData = deviceTestRunParameters.getTestRunData();
    StaticTestData testData = testRunData.getTestData();

    workParams.getToken().set(testRunInput.getToken());
    workParams.getWorkingDir().set(testRunInput.getWorkingDir());
    workParams.getClasspath().set(testRunInput.getClasspath().map(classpath -> classpath));

    // These defaults should come from the cli, but defaults are not exposed at the moment
    final var model = testRunInput.getDevice().orElse(EwDeviceSpec.DEFAULT_MODEL);
    final var version = testRunInput.getApiLevel().orElse(EwDeviceSpec.DEFAULT_VERSION);
    final var gpu = testRunInput.getGpu().orElse(EwDeviceSpec.DEFAULT_GPU);
    final var device = getObjectFactory().newInstance(EwDeviceSpec.class);
    device.getModel().set(model);
    device.getVersion().set(version);
    device.getGpu().set(gpu);

    if (testData.isLibrary()) {
      workParams.getLibraryTestApk().set(testData.getTestApk());
    } else {
      List<File> testedApks = testData.getTestedApkFinder().invoke(
        getDeviceConfigProvider(model, version)
      );
      if (testedApks.size() != 1) {
        throw new IllegalStateException("Expected exactly one tested APK, but found: " + testedApks.size());
      }
      workParams.getApks().set(Collections.singleton(testedApks.get(0)));
      workParams.getTestApk().set(testData.getTestApk());
    }

    // We have a single output dir, whereas GMD expects separate output dirs for:
    // a) test results - getOutputDirectory()
    // b) coverage - getCoverageOutputDirectory()
    // c) additional test output - getAdditionalTestOutputDir()
    // TODO: be a good citizen and split outputs after the test run
    workParams.getOutputsDir().set(testRunData.getOutputDirectory());

    workParams.getDevices().set(ProviderUtils.deviceToCliMap(getProviderFactory(), device).map(List::of));
    workParams.getInstrumentationRunner().set(testData.getInstrumentationRunner());
    workParams.getEnvironmentVariables().set(getProviderFactory().provider(testData::getInstrumentationRunnerArguments));
    // setting a ListProperty<OutputType> directly causes Gradle to barf with:
    // java.lang.IllegalArgumentException: Cannot set the value of a property of type java.util.List with element type wtf.emulator.OutputType using a provider with element type java.lang.Object.
    workParams.getOutputs().set(testRunInput.getOutputTypes().map(list -> list));
    workParams.getSecretEnvironmentVariables().set(testRunInput.getSecretEnvironmentVariables().map(map -> map));
    workParams.getRecordVideo().set(testRunInput.getRecordVideo());

    // infer orchestrator from additional apks if not set
    // TODO(madis) fix once there's a better API? https://issuetracker.google.com/issues/471349547
    var orchestratorApkCount = testRunData.getHelperApks().stream().filter(it -> it.getAbsolutePath().contains("orchestrator")).count();
    workParams.getUseOrchestrator().set(testRunInput.getUseOrchestrator().orElse(orchestratorApkCount > 0));

    workParams.getClearPackageData().set(testRunInput.getClearPackageData());
    workParams.getWithCoverage().set(testRunInput.getWithCoverage().map(
      withCoverage -> withCoverage || testData.isTestCoverageEnabled()
    ));

    workParams.getAdditionalApks().set(testRunInput.getAdditionalApks()
      .map(additionalApks -> additionalApks.plus(getObjectFactory().fileCollection().from(testRunData.getHelperApks()))
      )
    );
    workParams.getNumUniformShards().set(testRunInput.getNumUniformShards());
    workParams.getNumBalancedShards().set(testRunInput.getNumBalancedShards());
    workParams.getNumShards().set(testRunInput.getNumShards());
    workParams.getShardTargetRuntime().set(testRunInput.getShardTargetRuntime());
    workParams.getDirectoriesToPull().set(testRunInput.getDirectoriesToPull().map(list -> {
      // If baseline profiles / macrobenchmark is enabled, then let's pull the directories where the results are stored.
      List<String> mutableList = new ArrayList<>(list);
      if (isBaselineProfileEnabled(testData)) {
        // From https://issuetracker.google.com/issues/285187547#comment28
        mutableList.add("/storage/emulated/0/Android/media/" + testRunData.getTestData().getApplicationId());
      }
      return mutableList;
    }));
    workParams.getSideEffects().set(testRunInput.getSideEffects());
    workParams.getTimeout().set(testRunInput.getTestTimeout());
    workParams.getFileCacheEnabled().set(testRunInput.getFileCacheEnabled());
    workParams.getFileCacheTtl().set(testRunInput.getFileCacheTtl());
    workParams.getTestCacheEnabled().set(testRunInput.getTestCacheEnabled());
    workParams.getNumFlakyTestAttempts().set(testRunInput.getNumFlakyTestAttempts());
    workParams.getFlakyTestRepeatMode().set(testRunInput.getFlakyTestRepeatMode());
    workParams.getDisplayName().set(testRunInput.getDisplayName().orElse(testRunData.getTestRunId()));
    workParams.getDnsOverrides().set(testRunInput.getDnsOverrides().map(list -> list));
    workParams.getDnsServers().set(testRunInput.getDnsServers().map(list -> list));
    workParams.getEgressTunnel().set(testRunInput.getEgressTunnel());
    workParams.getEgressLocalhostForwardIp().set(testRunInput.getEgressLocalhostForwardIp());
    workParams.getRelays().set(testRunInput.getRelays().map(list -> list));
    workParams.getScmUrl().set(testRunInput.getScmUrl());
    workParams.getScmCommitHash().set(testRunInput.getScmCommitHash());
    workParams.getScmRefName().set(testRunInput.getScmRefName());
    workParams.getScmPrUrl().set(testRunInput.getScmPrUrl());
    workParams.getIgnoreFailures().set(testRunInput.getIgnoreFailures());
    workParams.getAsync().set(testRunInput.getAsync());
    workParams.getPrintOutput().set(testRunInput.getPrintOutput());
    workParams.getTestTargets().set(testRunInput.getTestTargets());
    workParams.getProxyHost().set(testRunInput.getProxyHost());
    workParams.getProxyPort().set(testRunInput.getProxyPort());
    workParams.getProxyUser().set(testRunInput.getProxyUser());
    workParams.getProxyPassword().set(testRunInput.getProxyPassword());
    workParams.getNonProxyHosts().set(testRunInput.getNonProxyHosts().map(list -> list));

    RegularFile outputFile = testRunInput.getIntermediatesOutputs().get().file(testRunData.getVariantName() + ".json");
    workParams.getOutputFile().set(outputFile);
    // TODO(tauno): using the project path here is technically incorrect, but it doesn't matter for sync cli invokes
    workParams.getTaskPath().set(testRunData.getProjectPath());

    return workParams;
  }

  private void convertResultsXml(@NotNull DeviceTestRunParameters<EwDeviceTestRunInput> deviceTestRunParameters) {
    // AGP expects the test results to be with a specific name (TEST-*.xml) in the output directory.
    // See /java/com/android/build/gradle/internal/test/report/TestReport.java;l=80
    File outputsDir = deviceTestRunParameters.getTestRunData().getOutputDirectory().getAsFile();
    if (outputsDir.exists()) {
      File resultsXml = new File(outputsDir, "results.xml");
      if (resultsXml.exists()) {
        File renamedResultsXml = new File(outputsDir, "TEST-results.xml");
        if (resultsXml.renameTo(renamedResultsXml)) {
          // Extract required metadata from deviceTestRunParameters
          String deviceName = deviceTestRunParameters.getTestRunData().getDeviceName();
          String flavor = deviceTestRunParameters.getTestRunData().getTestData().getFlavorName();
          String projectName = deviceTestRunParameters.getTestRunData().getProjectPath();

          addMetadataToResultsXml(renamedResultsXml, deviceName, flavor, projectName);
        } else {
          getLogger().error("Failed to rename results.xml to TEST-results.xml in {}", outputsDir.getAbsolutePath());
        }
      }
    }
  }

  private void addMetadataToResultsXml(File resultsXml, String deviceName, String flavor, String projectName) {
    // AGP expects the results.xml to contain metadata about the test run.
    // See java/com/android/build/gradle/internal/test/report/TestResult.java
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(resultsXml);

      Element rootElement = doc.getDocumentElement();

      // Create properties element if it doesn't exist
      Element propertiesElement;
      NodeList propertiesList = rootElement.getElementsByTagName("properties");
      if (propertiesList.getLength() > 0) {
        propertiesElement = (Element) propertiesList.item(0);
      } else {
        propertiesElement = doc.createElement("properties");
        rootElement.insertBefore(propertiesElement, rootElement.getFirstChild());
      }

      // Add device property
      Element deviceProperty = doc.createElement("property");
      deviceProperty.setAttribute("name", "device");
      deviceProperty.setAttribute("value", deviceName);
      propertiesElement.appendChild(deviceProperty);

      // Add flavor property
      Element flavorProperty = doc.createElement("property");
      flavorProperty.setAttribute("name", "flavor");
      flavorProperty.setAttribute("value", flavor);
      propertiesElement.appendChild(flavorProperty);

      // Add project property
      Element projectProperty = doc.createElement("property");
      projectProperty.setAttribute("name", "project");
      projectProperty.setAttribute("value", projectName);
      propertiesElement.appendChild(projectProperty);

      // Write out the modified content
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(resultsXml);
      transformer.transform(source, result);
    } catch (Exception e) {
      getLogger().warn("Error adding metadata to results XML: {}", e.getMessage());
    }
  }

  private Logger getLogger() {
    return Logging.getLogger(EwDeviceTestRunTaskAction.class);
  }

  private boolean isBaselineProfileEnabled(StaticTestData testData) {
    // A naive approach to detect if baseline profiles / macrobenchmark is enabled.
    return testData.getInstrumentationRunnerArguments().containsKey("androidx.benchmark.enabledRules");
  }

  private static DeviceConfigProvider getDeviceConfigProvider(@Nonnull Provider<DeviceModel> model, Provider<Integer> version) {
    return new DeviceConfigProvider() {

      @Override
      @Nonnull
      public String getConfigFor(String abi) {
        return abi;
      }

      @Override
      public int getDensity() {
        return model.get().getDensity();
      }

      @Override
      public String getLanguage() {
        return "en";
      }

      @Override
      public String getRegion() {
        return "US";
      }

      @Override
      @Nonnull
      public List<String> getAbis() {
        return switch (version.get()) {
          case 23, 24, 27, 28, 29, 30 -> List.of("x86");
          default -> List.of("x86_64");
        };
      }

      @Override
      public int getApiLevel() {
        return version.get();
      }

    };
  }

}
