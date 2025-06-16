package wtf.emulator;

import org.gradle.api.Project;

public enum EwProperties {
  ADD_REPOSITORY("addrepository");

  private static final String PREFIX = "wtf.emulator";

  private final String propName;

  EwProperties(String propName) {
    this.propName = propName;
  }

  public boolean getFlag(Project project, boolean defaultValue) {
    String value = project.getProviders().gradleProperty(PREFIX + "." + propName).getOrNull();
    if (value == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(value);
  }
}
