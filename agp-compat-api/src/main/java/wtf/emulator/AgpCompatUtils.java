package wtf.emulator;

import com.android.build.api.variant.AndroidTest;
import com.android.build.api.variant.HasAndroidTest;
import com.android.build.api.variant.Variant;

import javax.annotation.Nullable;

class AgpCompatUtils {
  @Nullable
  static AndroidTest getAndroidTest(Variant variant) {
    if (variant instanceof HasAndroidTest) {
      return ((HasAndroidTest) variant).getAndroidTest();
    }
    return null;
  }
}
