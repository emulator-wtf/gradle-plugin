package wtf.emulator.exec;

import com.android.build.api.variant.BuiltArtifact;
import com.android.build.api.variant.BuiltArtifacts;
import com.android.build.api.variant.BuiltArtifactsLoader;
import com.android.build.api.variant.FilterConfiguration;
import com.android.build.api.variant.VariantOutputConfiguration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EwWorkParameterUtils {
  /**
   * Wires an output apk from the folder into the output path, using the loader.
   */
  public static void configureApk(BuiltArtifactsLoader loader, ListProperty<Map<String, String>> devices, Provider<Directory> folder, Property<String> path) {
    if (folder.isPresent()) {
      final var apk = selectArtifact(loader, devices, folder.get());
      path.set(apk.getOutputFile());
    }
  }

  public static void configureApkFromFileCollection(BuiltArtifactsLoader loader, ListProperty<Map<String, String>> devices, Provider<FileCollection> files, Property<String> path) {
    if (files.isPresent()) {
      final var apk = selectArtifact(loader, devices, files.get());
      path.set(apk.getOutputFile());
    }
  }

  private static BuiltArtifact selectArtifact(BuiltArtifactsLoader loader, ListProperty<Map<String, String>> devices, Directory directory) {
    final var artifacts = loader.load(directory);
    if (artifacts == null) {
      throw new IllegalStateException("Artifacts folder " + directory + " does not exist");
    }

    return selectArtifact(devices, artifacts);
  }

  private static BuiltArtifact selectArtifact(BuiltArtifactsLoader loader, ListProperty<Map<String, String>> devices, FileCollection files) {
    final var artifacts = loader.load(files);
    if (artifacts == null) {
      throw new IllegalStateException("Failed to load artifacts does not exist");
    }

    return selectArtifact(devices, artifacts);
  }

  private static BuiltArtifact selectArtifact(ListProperty<Map<String, String>> devices, BuiltArtifacts artifacts) {
    final var elements = artifacts.getElements();

    // prefer single if present
    final var single = elements.stream()
      .filter(it -> it.getOutputType().equals(VariantOutputConfiguration.OutputType.SINGLE))
      .filter(it -> it.getFilters().isEmpty())
      .findFirst();
    if (single.isPresent()) {
      return single.get();
    }

    // figure out architecture from devices
    final var versions = devices.get().stream().map(it -> Integer.parseInt(it.get("version"))).collect(Collectors.toSet());
    final var architectures = new HashSet<>(versions.stream().map(EwWorkParameterUtils::versionToArchitecture).toList());
    if (architectures.isEmpty()) {
      architectures.add("x86"); // default is api 30, x86
    }

    // find _an_ apk that matches all architectures
    final var matchingSplits = elements.stream()
      .filter(artifact -> architectures.stream().allMatch(arch -> {
        var match = artifact.getFilters().stream().filter(it ->
          (it.getFilterType() == FilterConfiguration.FilterType.ABI && arch.equals(it.getIdentifier()))
        ).findFirst();
        return match.isPresent();
      }))
      .findFirst();

    // universal fallback
    final var universal = elements.stream()
      .filter(it -> it.getOutputType().equals(VariantOutputConfiguration.OutputType.UNIVERSAL))
      .filter(it -> it.getFilters().isEmpty())
      .findFirst();

    return matchingSplits
      .or(() -> universal)
      .orElseThrow(() -> new IllegalArgumentException(buildArchitectureMismatchMessage(architectures)));
  }

  private static String buildArchitectureMismatchMessage(Set<String> desiredArchitectures) {
    if (desiredArchitectures.size() > 1) {
      return "No matching apk split found that satisfies required architectures: " + String.join(", ", desiredArchitectures) + ".\n" +
        "You may need to enable universal APK generation in your build configuration.";
    }

    // we will always have exactly one architecture here
    final var arch = desiredArchitectures.iterator().next();

    return "No matching apk split found for " + arch + ".\n" +
      "You may need to enable an apk split for this architecture or turn on universal APK generation in your build configuration.";
  }

  private static String versionToArchitecture(int version) {
    // TODO(madis) hardcoding this mapping is not very nice
    // these versions are stuck on x86
    if (version >= 23 && version <= 24 || version >= 27 && version <= 30) {
      return "x86";
    }
    return "x86_64";
  }
}
