plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
  alias(libs.plugins.protobuf)
}

dependencies {
  compileOnly(libs.gradle.api.v80)
  compileOnly(libs.protobuf.java)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}

protobuf {
  protoc {
    artifact = libs.protoc.get().toString()
  }

  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        java { }
      }
    }
  }
}
