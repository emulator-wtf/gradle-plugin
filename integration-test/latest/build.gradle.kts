plugins {
  alias(libs.plugins.android.application)
  id("wtf.emulator.gradle")
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

import wtf.emulator.ewDevices
import wtf.emulator.DeviceModel

android {
  compileSdk = 33

  defaultConfig {
    applicationId = "wtf.emulator.sample"
    minSdk = 23
    targetSdk = 33
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
  androidTestImplementation("androidx.test:rules:1.6.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
  androidTestImplementation("androidx.test:core:1.6.1")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
  androidTestImplementation("com.google.truth:truth:1.4.4")
}
