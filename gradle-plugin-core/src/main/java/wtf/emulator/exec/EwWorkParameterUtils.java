package wtf.emulator.exec;

import com.android.build.api.variant.BuiltArtifact;
import com.android.build.api.variant.BuiltArtifacts;
import com.android.build.api.variant.BuiltArtifactsLoader;
import com.android.build.api.variant.VariantOutputConfiguration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class EwWorkParameterUtils {
  /**
   * Wires an output apk from folder into the output path, using the loader.
   */
  public static void configureApk(BuiltArtifactsLoader loader, Provider<Directory> folder, Property<String> path) {
    if (folder.isPresent()) {
      final var apk = selectArtifact(loader, folder.get());
      path.set(apk.getOutputFile());
    }
  }

  public static void configureApkFromFileCollection(BuiltArtifactsLoader loader, Provider<FileCollection> files, Property<String> path) {
    if (files.isPresent()) {
      final var apk = selectArtifact(loader, files.get());
      path.set(apk.getOutputFile());
    }
  }

  private static BuiltArtifact selectArtifact(BuiltArtifactsLoader loader, Directory directory) {
    final var artifacts = loader.load(directory);
    if (artifacts == null) {
      throw new IllegalStateException("Artifacts folder " + directory + " does not exist");
    }

    return selectArtifact(artifacts);
  }

  private static BuiltArtifact selectArtifact(BuiltArtifactsLoader loader, FileCollection files) {
    final var artifacts = loader.load(files);
    if (artifacts == null) {
      throw new IllegalStateException("Failed to load artifactsdoes not exist");
    }

    return selectArtifact(artifacts);
  }

  private static BuiltArtifact selectArtifact(BuiltArtifacts artifacts) {
    final var elements = artifacts.getElements();

    final var single = elements.stream()
      .filter(it -> it.getOutputType().equals(VariantOutputConfiguration.OutputType.SINGLE))
      .filter(it -> it.getFilters().isEmpty())
      .findFirst();

    final var universal = elements.stream()
      .filter(it -> it.getOutputType().equals(VariantOutputConfiguration.OutputType.UNIVERSAL))
      .filter(it -> it.getFilters().isEmpty())
      .findFirst();

    return single.or(() -> universal).orElseThrow(() -> new IllegalStateException("Variant has no fitting outputs!"));
  }
}
