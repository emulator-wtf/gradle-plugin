package wtf.emulator;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.BaseVariant;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("deprecation")
public record Agp81DataHolder(AtomicReference<BaseExtension> extension, Map<String, BaseVariant> data) implements AgpVariantDataHolder {}
