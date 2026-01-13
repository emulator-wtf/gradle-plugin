package wtf.emulator.setup;

import java.util.Locale;

class StringUtils {
  static String capitalize(String str) {
    if (str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
  }
}
