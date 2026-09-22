package wtf.emulator;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public enum EwEnv {
  API_TOKEN("EW_API_TOKEN"),
  SCM_URL("EW_SCM_URL"),
  SCM_COMMIT("EW_SCM_COMMIT"),
  SCM_REF_NAME("EW_SCM_REF_NAME"),
  SCM_PR_URL("EW_SCM_PR_URL"),
  ;

  private final String envKey;

  EwEnv(String envKey) {
    this.envKey = envKey;
  }

  public Provider<String> getStringProvider(Project project) {
    return project.getProviders().environmentVariable(envKey);
  }
}
