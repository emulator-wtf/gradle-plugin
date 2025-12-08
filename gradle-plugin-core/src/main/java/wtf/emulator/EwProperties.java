package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public enum EwProperties {
  ADD_REPOSITORY("addrepository"),
  ADD_RUNTIME_DEPENDENCY("addruntimedependency"),
  DEBUG("debug"),
  CONNECTIVITY_CHECK("connectivitycheck");

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

  public Provider<Boolean> getFlagProvider(Project project, boolean defaultValue) {
    return project.getProviders().gradleProperty(PREFIX + "." + propName)
        .map(Boolean::parseBoolean)
        .orElse(defaultValue);
  }
}
