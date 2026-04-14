import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.emulatorwtf)
}

kotlin {
  android {
    compileSdk = 36
    namespace = "wtf.emulator.sample"
    minSdk = 23

    // needed to pick up java tests
    withJava()

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }

    withDeviceTest {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  sourceSets {
    named("androidDeviceTest") {
      dependencies {
        implementation(libs.androidx.rules)
        implementation(libs.androidx.runner)
        implementation(libs.androidx.core)
        implementation(libs.androidx.junit.ktx)
      }
    }
  }
}
