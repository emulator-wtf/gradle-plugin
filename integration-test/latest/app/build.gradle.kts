import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import wtf.emulator.ewDevices
import wtf.emulator.DeviceModel


plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.baselineprofile)
  alias(libs.plugins.integration.test.conventions)
  alias(libs.plugins.emulatorwtf)
}

android {
  kotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
  }

  compileSdk = 36

  defaultConfig {
    applicationId = "wtf.emulator.sample"
    minSdk = 23
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableAndroidTestCoverage = false
    }
  }

  namespace = "wtf.emulator.sample"

  testOptions.managedDevices.ewDevices {
    register("ewPixel7api33") {
      device = DeviceModel.PIXEL_7
      apiLevel = 33
    }
  }
}

emulatorwtf {
  targets {
    testMethod("MinimalTests", "otherTest")
  }
}

dependencies {
  androidTestImplementation(libs.androidx.rules)
  androidTestImplementation(libs.androidx.runner)
  androidTestImplementation(libs.androidx.core)
  androidTestImplementation(libs.androidx.junit.ktx)
  "baselineProfile"(project(":baselineprofile"))
}
