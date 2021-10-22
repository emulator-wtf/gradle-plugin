plugins {
  `java-gradle-plugin`
  `maven-publish`
}

repositories {
  mavenCentral()
  google()
}

gradlePlugin {
  plugins {
    create("ewPlugin") {
      id = "wtf.emulator.gradle"
      implementationClass = "wtf.emulator.EwPlugin"
    }
  }
}

tasks.compileJava {
  options.release.set(8)
}

dependencies {
  compileOnly("com.android.tools.build:gradle:4.0.0")
}

version = "0.0.1"
group = "wtf.emulator"
