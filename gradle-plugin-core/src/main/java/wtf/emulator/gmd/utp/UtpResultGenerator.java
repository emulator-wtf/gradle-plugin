package wtf.emulator.gmd.utp;

import wtf.emulator.utp.Artifact;
import wtf.emulator.utp.ArtifactType;
import wtf.emulator.utp.Label;
import wtf.emulator.utp.Path;
import wtf.emulator.utp.TestResult;
import wtf.emulator.utp.TestSuiteResult;

import java.util.List;

public class UtpResultGenerator {

  private static final String PROFILE_LABEL = "additionaltestoutput.benchmark.trace";

  // TODO: This is a minimal implementation to generate UTP results that would satisfy the data that the baselineprofile plugin expects.
  //  See: benchmark/baseline-profile-gradle-plugin/src/main/kotlin/androidx/baselineprofile/gradle/producer/tasks/CollectBaselineProfileTask.kt
  //  This should be extended to include the full test run details (test cases, statuses, logcat, etc)
  public static TestSuiteResult generateUtpResults(List<java.nio.file.Path> benchmarkFiles) {
    TestSuiteResult.Builder testSuiteResultBuilder = TestSuiteResult.newBuilder();
    TestResult.Builder testResultBuilder = TestResult.newBuilder();

    benchmarkFiles.forEach(path -> testResultBuilder.addOutputArtifact(createOutputArtifact(PROFILE_LABEL, path.toAbsolutePath().toString())));

    testSuiteResultBuilder.addTestResult(testResultBuilder.build());
    return testSuiteResultBuilder.build();
  }

  private static Artifact createOutputArtifact(String labelText, String filePath) {
    Label label = Label.newBuilder()
      .setLabel(labelText)
      .setNamespace("android")
      .build();

    Path sourcePath = Path.newBuilder()
      .setPath(filePath)
      .build();

    Artifact.Builder artifactBuilder = Artifact.newBuilder()
      .setLabel(label)
      .setSourcePath(sourcePath)
      .setType(ArtifactType.TEST_DATA);

    return artifactBuilder.build();
  }
}
