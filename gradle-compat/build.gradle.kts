plugins {
  id("wtf.emulator.java")
}

repositories {
  mavenCentral()
  google()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:6.1.1")

  api(project(":gradle-compat-api"))

  implementation(project(":gradle-compat-6-1"))
  implementation(project(":gradle-compat-6-6"))
  implementation(project(":gradle-compat-7-6"))
  implementation("com.vdurmont:semver4j:3.1.0")
}
