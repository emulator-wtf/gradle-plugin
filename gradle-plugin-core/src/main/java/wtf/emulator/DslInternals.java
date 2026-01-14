package wtf.emulator;

import org.gradle.api.Action;

public class DslInternals {
  public static Action<EwVariantFilter> getFilter(EwInvokeDsl dsl) {
    return dsl.getFilter();
  }
}
