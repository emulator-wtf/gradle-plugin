import wtf.emulator.ewDevices
import wtf.emulator.TestReporter
import wtf.emulator.DeviceModel

plugins {
  alias(libs.plugins.android.application)
  id("wtf.emulator.gradle")
  id("wtf.emulator.test.integration-test-conventions")
  alias(libs.plugins.baselineprofile)
}

android {
  compileSdk = 34

  defaultConfig {
    applicationId = "wtf.emulator.sample"
    minSdk = 23
    targetSdk = 34
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

dependencies {
  implementation(project(":library"))
  implementation(libs.androidx.profileinstaller)

  androidTestImplementation("androidx.test:rules:1.6.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
  androidTestImplementation("androidx.test:core:1.6.1")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
  androidTestImplementation("com.google.truth:truth:1.4.4")

  testImplementation("junit:junit:4.13.2")
  "baselineProfile"(project(":baselineprofile"))
}

emulatorwtf {
  testReporters = listOf(TestReporter.GRADLE_TEST_REPORTING_API)
  device {
    model = DeviceModel.PIXEL_2
    version = 27
  }
}
