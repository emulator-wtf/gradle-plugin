plugins {
  `java-gradle-plugin`
  id("wtf.emulator.java")
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.lint)
}

gradlePlugin {
  plugins {
    create("ewPlugin") {
      id = "wtf.emulator.gradle"
      implementationClass = "wtf.emulator.EwPlugin"
    }
  }
}

dependencies {
  compileOnly(libs.agp)
  compileOnly(libs.autovalue.annotations)

  api(libs.jsr305)

  annotationProcessor(libs.autovalue.compiler)
  annotationProcessor(libs.autovalue.gson.extension)
  annotationProcessor(libs.autovalue.gson.factory)

  implementation(projects.gradleCompat)
  implementation(libs.semver4j)
  implementation(libs.json)
  implementation(libs.gson)
  implementation(libs.commons.io)
  implementation(libs.autovalue.gson.runtime)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}

lint {
  baseline = file("src/main/lint/baseline.xml")
}

buildConfig {
  packageName("wtf.emulator")
  buildConfigField("String", "VERSION", "\"${providers.gradleProperty("VERSION_NAME").orNull ?: project.version}\"")
}
