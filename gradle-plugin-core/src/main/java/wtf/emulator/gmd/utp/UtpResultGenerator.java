package wtf.emulator.gmd.utp;

import com.google.testing.platform.proto.api.core.LabelProto;
import com.google.testing.platform.proto.api.core.PathProto;
import com.google.testing.platform.proto.api.core.TestArtifactProto;
import com.google.testing.platform.proto.api.core.TestResultProto;
import com.google.testing.platform.proto.api.core.TestSuiteResultProto;

import java.nio.file.Path;
import java.util.List;

public class UtpResultGenerator {

  private static final String PROFILE_LABEL = "additionaltestoutput.benchmark.trace";

  // TODO: This is a minimal implementation to generate UTP results that would satisfy the data that the baselineprofile plugin expects.
  //  See: benchmark/baseline-profile-gradle-plugin/src/main/kotlin/androidx/baselineprofile/gradle/producer/tasks/CollectBaselineProfileTask.kt
  //  This should be extended to include the full test run details (test cases, statuses, logcat, etc)
  public static TestSuiteResultProto.TestSuiteResult generateUtpResults(List<Path> benchmarkFiles) {
    TestSuiteResultProto.TestSuiteResult.Builder testSuiteResultBuilder = TestSuiteResultProto.TestSuiteResult.newBuilder();
    TestResultProto.TestResult.Builder testResultBuilder = TestResultProto.TestResult.newBuilder();

    benchmarkFiles.forEach(path -> testResultBuilder.addOutputArtifact(createOutputArtifact(PROFILE_LABEL, path.toAbsolutePath().toString())));

    testSuiteResultBuilder.addTestResult(testResultBuilder.build());
    return testSuiteResultBuilder.build();
  }

  private static TestArtifactProto.Artifact createOutputArtifact(String labelText, String filePath) {
    LabelProto.Label label = LabelProto.Label.newBuilder()
      .setLabel(labelText)
      .setNamespace("android")
      .build();

    PathProto.Path sourcePath = PathProto.Path.newBuilder()
      .setPath(filePath)
      .build();

    TestArtifactProto.Artifact.Builder artifactBuilder = TestArtifactProto.Artifact.newBuilder()
      .setLabel(label)
      .setSourcePath(sourcePath)
      .setType(TestArtifactProto.ArtifactType.TEST_DATA);

    return artifactBuilder.build();
  }
}
