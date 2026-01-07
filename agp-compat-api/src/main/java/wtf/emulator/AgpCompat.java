package wtf.emulator;

import com.android.build.api.variant.TestVariant;
import com.android.build.api.variant.Variant;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public interface AgpCompat {
  AgpVariantDataHolder collectAgpVariantData(Project target);

  void populateAgpVariantData(AgpVariantDataHolder holder, Variant variant, AgpVariantData out);

  @Nullable
  Provider<FileCollection> getTestedApks(Project project, TestVariant testVariant);

  @Nullable
  Provider<Directory> getTestedApkDirectory(Project project, TestVariant testVariant);
}
