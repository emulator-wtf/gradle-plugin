plugins {
  id("wtf.emulator.java")
  alias(libs.plugins.lint)
  alias(libs.plugins.protobuf)
}

dependencies {
  compileOnly(pinned.gradle.api.v80)
  compileOnly(pinned.protobuf.java)

  testImplementation(libs.bundles.test)

  lintChecks(libs.lint.gradle)
}

protobuf {
  protoc {
    artifact = pinned.protoc.get().toString()
  }

  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        java { }
      }
    }
  }
}
