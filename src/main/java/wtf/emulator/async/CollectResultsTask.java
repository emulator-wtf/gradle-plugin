package wtf.emulator.async;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import wtf.emulator.OutputType;
import wtf.emulator.exec.EwCollectResultsWorkParameters;

import javax.inject.Inject;
import java.util.List;

public abstract class CollectResultsTask extends DefaultTask {
  @Classpath
  @InputFiles
  public abstract Property<FileCollection> getClasspath();

  @Optional
  @OutputDirectory
  public abstract DirectoryProperty getOutputsDir();

  @Optional
  @Input
  public abstract ListProperty<OutputType> getOutputTypes();

  @Optional
  @Input
  public abstract Property<Boolean> getPrintOutput();

  @Internal
  public abstract Property<EwAsyncExecService> getExecService();

  @Inject
  public abstract ObjectFactory getObjectFactory();

  @TaskAction
  public void collectResults() {
    EwCollectResultsWorkParameters params = getObjectFactory().newInstance(EwCollectResultsWorkParameters.class);

    params.getClasspath().set(getClasspath());
    params.getOutputsDir().set(getOutputsDir());
    params.getOutputs().set(getOutputTypes());
    params.getPrintOutput().set(getPrintOutput());

    List<String> messages = getExecService().get().drainResults(params);

    System.out.println("\n\nEMULATOR.WTF TEST RESULTS:");
    System.out.println("==========================\n");
    messages.forEach(System.out::println);
    System.out.println("==========================\n\n");
  }
}
