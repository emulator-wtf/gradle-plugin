package wtf.emulator;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.testing.TestEventReporterFactory;

import javax.inject.Inject;

public abstract class TestReportingDepsHolder {

  @Inject
  public abstract ProjectLayout getLayout();

  @Inject
  public abstract TestEventReporterFactory getTestEventReporterFactory();

}
