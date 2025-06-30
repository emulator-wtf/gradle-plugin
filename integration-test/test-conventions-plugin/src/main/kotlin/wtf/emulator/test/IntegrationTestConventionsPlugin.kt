package wtf.emulator.test

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import java.io.File

class IntegrationTestConventionsPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.tasks.register("validateBaselineProfile") { task ->

      task.group = "verification"
      task.description = "Fails if baseline profile files are missing or empty"

      val profilesDirProvider = project.layout.projectDirectory.dir("src/release/generated/baselineProfiles")

      task.dependsOn(project.tasks.named("generateBaselineProfile"))

      task.inputs.dir(profilesDirProvider)
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withNormalizer(org.gradle.api.tasks.ClasspathNormalizer::class.java)

      task.doLast {
        val actualProfilesDir = profilesDirProvider.asFile

        val filesToCheck = listOf("baseline-prof.txt", "startup-prof.txt")

        filesToCheck.forEach { fileName ->
          val file = File(actualProfilesDir, fileName)
          if (!file.exists()) {
            throw GradleException("❌ $fileName was not generated in directory: ${actualProfilesDir.absolutePath}")
          }
          if (file.length() == 0L) {
            throw GradleException("❌ $fileName is empty in directory: ${actualProfilesDir.absolutePath}")
          }
        }
        // Use Gradle's logger for output
        task.logger.lifecycle("✅ Baseline profiles look good in ${actualProfilesDir.absolutePath}")
      }
    }
  }
}
