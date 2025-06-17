plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.lint)
}

dependencies {
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.agp)

  compileOnly(libs.autovalue.annotations)

  api(libs.jsr305)

  annotationProcessor(libs.autovalue.compiler)
  annotationProcessor(libs.autovalue.gson.extension)
  annotationProcessor(libs.autovalue.gson.factory)

  implementation(libs.semver4j)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}

buildConfig {
  packageName("wtf.emulator")

  // version of the Gradle plugin
  buildConfigField("VERSION", providers.gradleProperty("VERSION_NAME").orElse(project.version.toString()))

  // Maven coordinates for cli without version
  buildConfigField("EW_CLI_MODULE", libs.emulatorwtf.cli.map { it.module.toString() })

  // Cli version to use by default
  buildConfigField("EW_CLI_VERSION", libs.emulatorwtf.cli.map { it.version!! })

  // Full maven coordinates for runtime, including version
  buildConfigField("EW_RUNTIME_COORDS", libs.emulatorwtf.runtime.map { it.toString() })
}
