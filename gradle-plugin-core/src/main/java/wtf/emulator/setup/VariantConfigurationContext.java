package wtf.emulator.setup;

import com.android.build.api.artifact.SingleArtifact;
import com.android.build.api.variant.AndroidTest;
import com.android.build.api.variant.ApplicationVariant;
import com.android.build.api.variant.LibraryVariant;
import com.android.build.api.variant.TestVariant;
import com.android.build.api.variant.Variant;
import com.google.auto.value.AutoValue;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import wtf.emulator.AgpCompat;
import wtf.emulator.AgpVariantDataHolder;
import wtf.emulator.EwInvokeDsl;

import javax.annotation.Nullable;

@AutoValue abstract class VariantConfigurationContext {
  abstract AgpCompat compat();
  abstract AgpVariantDataHolder holder();
  abstract String invokeName();
  abstract EwInvokeDsl dsl();
  abstract Variant variant();
  @Nullable abstract Provider<Directory> appApksFolder();
  @Nullable abstract Provider<FileCollection> appApkFiles();
  @Nullable abstract Provider<Directory> testApksFolder();
  @Nullable abstract Provider<Directory> libraryTestApksFolder();
  abstract Builder toBuilder();

  public static VariantConfigurationContext.Builder builder(AgpCompat compat, AgpVariantDataHolder holder, String invokeName, EwInvokeDsl dsl) {
    return new AutoValue_VariantConfigurationContext.Builder()
      .compat(compat)
      .holder(holder)
      .invokeName(invokeName)
      .dsl(dsl);
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder compat(AgpCompat compat);
    abstract AgpCompat compat();

    abstract Builder holder(AgpVariantDataHolder holder);
    abstract Builder invokeName(String invokeName);
    abstract Builder dsl(EwInvokeDsl dsl);
    abstract Builder variant(Variant variant);
    abstract Builder appApksFolder(@Nullable Provider<Directory> appApksFolder);
    abstract Builder appApkFiles(@Nullable Provider<FileCollection> appApkFiles);
    abstract Builder testApksFolder(Provider<Directory> testApksFolder);
    abstract Builder libraryTestApksFolder(Provider<Directory> libraryTestApksFolder);
    abstract VariantConfigurationContext build();

    public Builder app(ApplicationVariant variant, AndroidTest testVariant) {
      variant(variant);
      appApksFolder(variant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
      testApksFolder(testVariant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
      return this;
    }

    public Builder library(LibraryVariant variant, AndroidTest testVariant) {
      variant(variant);
      // library projects only have the test apk
      libraryTestApksFolder(testVariant.getArtifacts().get(SingleArtifact.APK.INSTANCE));
      return this;
    }

    public Builder test(Project project, TestVariant variant) {
      variant(variant);
      testApksFolder(variant.getArtifacts().get(SingleArtifact.APK.INSTANCE));

      // look up the referenced target apks
      final var targetApkDir = compat().getTestedApkDirectory(project, variant);
      if (targetApkDir != null) {
        appApksFolder(targetApkDir);
      } else {
        final var targetApkCollection = compat().getTestedApks(project, variant);
        if (targetApkCollection == null) {
          throw new IllegalStateException("Could not configure target app apk for test module " + project.getPath());
        }
        appApkFiles(targetApkCollection);
      }

      return this;
    }
  }
}
