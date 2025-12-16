package wtf.emulator;

import com.google.gson.annotations.SerializedName;

public enum GpuMode {
  /**
   * Use software rendering. Use this if you need deterministic pixel-perfect rendering, e.g. for
   * screenshot testing.
   */
  @SerializedName("software")
  SOFTWARE("software"),

  /**
   * Use hardware rendering if available. Typically, this is available, but there may
   * be rare cases where no machines with GPU acceleration are available in our workload cluster.
   * In such cases, the emulator may still use software rendering.
   */
  @SerializedName("auto")
  AUTO("auto");

  private final String cliValue;

  GpuMode(String cliValue) {
    this.cliValue = cliValue;
  }

  public String getCliValue() {
    return cliValue;
  }
}
