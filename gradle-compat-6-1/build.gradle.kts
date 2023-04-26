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
}
