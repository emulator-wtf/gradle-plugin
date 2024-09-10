package wtf.emulator;

public enum OutputType {
  SUMMARY("summary"),
  MERGED_RESULTS_XML("merged_results_xml"),
  COVERAGE("coverage"),
  PULLED_DIRS("pulled_dirs"),
  RESULTS_XML("results_xml"),
  LOGCAT("logcat"),
  CAPTURED_VIDEO("captured_video"),
  INDIVIDUAL_TEST_VIDEOS("individual_test_videos");

  private final String typeName;

  OutputType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }
}
