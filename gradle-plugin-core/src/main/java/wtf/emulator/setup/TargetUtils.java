package wtf.emulator.setup;

import org.gradle.api.provider.ListProperty;
import wtf.emulator.TestTargetsSpec;

import java.util.ArrayList;
import java.util.List;

public class TargetUtils {
  public static String toCliString(TestTargetsSpec targetsSpec) {
    StringBuilder sb = new StringBuilder();
    appendCliStringTarget(sb, "package", targetsSpec.getPackages());
    appendCliStringTarget(sb, "notPackage", targetsSpec.getExcludePackages());
    appendCliStringTarget(sb, "class", targetsSpec.getClasses(), targetsSpec.getMethods());
    appendCliStringTarget(sb, "notClass", targetsSpec.getExcludeClasses(), targetsSpec.getExcludeMethods());
    appendCliStringTarget(sb, "annotation", targetsSpec.getAnnotations());
    appendCliStringTarget(sb, "notAnnotation", targetsSpec.getExcludeAnnotations());
    appendCliStringTarget(sb, "filter", targetsSpec.getFilters());

    if (targetsSpec.getSize().isPresent()) {
      if (!sb.isEmpty()) {
        sb.append(";");
      }
      sb.append("size ").append(targetsSpec.getSize().get().getCliValue());
    }

    if (targetsSpec.getRegex().isPresent()) {
      if (!sb.isEmpty()) {
        sb.append(";");
      }
      sb.append("regex ").append(targetsSpec.getRegex().get());
    }

    return sb.toString();
  }

  private static void appendCliStringTarget(StringBuilder out, String operatorName, ListProperty<?>... targets) {
    int sz = 0;
    for (ListProperty<?> target : targets) {
      if (target.isPresent()) {
        sz += target.get().size();
      }
    }
    if (sz == 0) {
      return;
    }

    List<String> all = new ArrayList<>(sz);
    for (ListProperty<?> target : targets) {
      if (target.isPresent()) {
        for (Object item : target.get()) {
          all.add(item.toString());
        }
      }
    }

    if (!out.isEmpty()) {
      out.append(";");
    }

    out.append(operatorName).append(" ");
    out.append(String.join(",", all));
  }
}
