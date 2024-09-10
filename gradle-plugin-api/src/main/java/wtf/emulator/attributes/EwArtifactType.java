package wtf.emulator.attributes;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface EwArtifactType extends Named {
  Attribute<EwArtifactType> EW_ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("wtf.emulator.artifact.type", EwArtifactType.class);

  String SUMMARY_JSON = "summary-json";
}
