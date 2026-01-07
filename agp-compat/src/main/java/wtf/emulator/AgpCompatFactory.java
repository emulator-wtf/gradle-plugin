package wtf.emulator;

import com.android.build.api.AndroidPluginVersion;

public class AgpCompatFactory {
  private static final AndroidPluginVersion AGP_8_5_0 = (new AndroidPluginVersion(8, 5));

  public static AgpCompat getAgpCompat(AndroidPluginVersion agpVersion) {
    if (agpVersion.compareTo(AGP_8_5_0) >= 0) {
      return new AgpCompat_8_5();
    }
    return new AgpCompat_8_1();
  }
}
