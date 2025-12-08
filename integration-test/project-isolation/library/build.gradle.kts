plugins {
  alias(libs.plugins.android.library)
  id("wtf.emulator.gradle")
}

import wtf.emulator.ewDevices
import wtf.emulator.DeviceModel

android {
  compileSdk = 34

  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableAndroidTestCoverage = false
    }
  }

  namespace = "wtf.emulator.sample.library"

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

emulatorwtf {
  device {
    model = DeviceModel.PIXEL_2
    version = 27
  }
}
