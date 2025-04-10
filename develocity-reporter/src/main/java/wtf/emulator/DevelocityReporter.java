package wtf.emulator;

import com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports;
import com.gradle.develocity.agent.gradle.test.JUnitXmlDialect;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.util.function.Function;

public class DevelocityReporter {
  public static <T extends Task> void configure(Project target, TaskProvider<T> testTask, Function<T, Provider<RegularFile>> mergedXmlProvider) {
    TaskProvider<ImportJUnitXmlReports> reportTask = ImportJUnitXmlReports.register(target.getTasks(), testTask, JUnitXmlDialect.ANDROID_FIREBASE);
    reportTask.configure(task -> task.getReports().setFrom(testTask.flatMap(mergedXmlProvider::apply)));
  }
}
