plugins {
  alias(libs.plugins.android.test)
  id("wtf.emulator.gradle")
}

import wtf.emulator.DeviceModel

android {
  compileSdk = 34

  targetProjectPath = ":app"

  defaultConfig {
    minSdk = 23
    targetSdk = 34
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableAndroidTestCoverage = false
    }
  }

  namespace = "wtf.emulator.sample.test"
}

dependencies {
  implementation("wtf.emulator:test-runtime-android:0.2.0")
  implementation("androidx.test:rules:1.6.1")
  implementation("androidx.test:runner:1.6.2")
  implementation("androidx.test:core:1.6.1")
  implementation("androidx.test.ext:junit-ktx:1.2.1")
  implementation("com.google.truth:truth:1.4.4")
}

emulatorwtf {
  device {
    model = DeviceModel.PIXEL_2
    apiLevel = 27
  }
}
