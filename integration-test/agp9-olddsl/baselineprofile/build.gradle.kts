import wtf.emulator.ewDevices
import wtf.emulator.DeviceModel

plugins {
  alias(libs.plugins.android.test)
  alias(libs.plugins.baselineprofile)
  alias(libs.plugins.emulatorwtf)
}

android {
  namespace = "wtf.emulator.baselineprofile"
  compileSdk = 36

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  defaultConfig {
    minSdk = 28
    targetSdk = 36

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  targetProjectPath = ":app"

  // This code creates the gradle managed device used to generate baseline profiles.
  // To use GMD please invoke generation through the command line:
  // ./gradlew :app:generateBaselineProfile
  testOptions.managedDevices.ewDevices {
    register("ewPixel7api33") {
      device = DeviceModel.PIXEL_7
      apiLevel = 33
    }
  }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
  managedDevices += "ewPixel7api33"
  useConnectedDevices = false
}

dependencies {
  implementation(libs.androidx.junit)
  implementation(libs.androidx.espresso.core)
  implementation(libs.androidx.uiautomator)
  implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
  onVariants { v ->
    val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
    v.instrumentationRunnerArguments.put(
      "targetAppId",
      v.testedApks.map { artifactsLoader.load(it)?.applicationId }
    )
  }
}

emulatorwtf {
  variantFilter {
    setEnabled(false)
  }
}
