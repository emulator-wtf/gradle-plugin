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

  implementation(projects.gradleCompat)
  implementation(libs.semver4j)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}

buildConfig {
  packageName("wtf.emulator")
  buildConfigField("String", "VERSION", "\"${providers.gradleProperty("VERSION_NAME").orNull ?: project.version}\"")
}
