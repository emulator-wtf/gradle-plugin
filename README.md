# emulator.wtf Gradle Plugin

Emulator.wtf is an Android cloud emulator laser-focused on performance to
deliver quick feedback to your PRs.

With this Gradle plugin you can run your Android instrumentation tests with
[emulator.wtf](https://emulator.wtf).

## Running

The plugin will add new Gradle tasks for each testable Android variant with the
name `test${variant.name.capitalize()}WithEmulatorWtf`. A few examples:

- `testDebugWithEmulatorWtf` - a simple app with `debug` and `release`
  buildtypes
- `testFreeDebugWithEmulatorWtf`, `testPaidDebugWithEmulatorWtf` - an app having
  a single flavor dimension with `free` and `paid`.

You can always run `./gradlew :app:tasks` to see the added tasks, they will be
listed under the _Verification_ section.

## Installation

### Using plugins DSL (`plugins {}`)

Make sure `mavenCentral()` repository is in your `settings.gradle` file:

```groovy
pluginManagement {
  repositories {
    mavenCentral()
    maven { url "https://plugins.gradle.org/m2/" }
  }
}
```

And then you can enable the plugin by adding it to the `plugins` block of your
app project (usually under `app/build.gradle`):

```groovy
plugins {
  id "wtf.emulator.gradle" version "0.0.11"
}
```

### Using `buildscript` classpath (`apply plugin`)

Add the emulator.wtf plugin to your `buildscript` classpath in the root
`build.gradle` file:

```groovy
buildscript {
  repositories {
    google()
    mavenCentral()
  }
  
  dependencies {
    // ... other deps here, like com.android.tools.build:gradle
    classpath "wtf.emulator:gradle-plugin:0.0.11"
  }
}
```

You can then apply the plugin in your app project (usually in `app/build.gradle`):

```groovy
apply plugin: 'wtf.emulator.gradle'
```

### Token

To run tests you'll need to pass your API token to the Gradle plugin. The
recommended way to do so is via defining a `EW_API_TOKEN` environment variable,
it'll get picked up by the plugin automatically. This way you don't have to
worry about passing the token to your Gradle build.

Alternatively, you can use the `token` option on the `emulatorwtf` extension.
For example, to grab the token via a `ewApiToken` project property:

```groovy
emulatorwtf {
  token = project.properties.ewApiToken
}
```

NOTE: hardcoding API tokens in your `build.gradle` files is not recommended.

## Configuration

The `emulatorwtf` plugin DSL supports the following configuration options:

```groovy
emulatorwtf {
  // CLI version to use, defaults to 0.0.46
  version = '0.0.46'

  // emulator.wtf API token, we recommend either using the EW_API_TOKEN env var
  // instead of this or passing this value in via a project property
  token = 'AQAA...'

  // where to store results in, they will be further scoped by the variant name,
  // i.e. ./gradlew :app:testFreeDebugWithEmulatorWtf will store outputs in
  // build/build-results/freeDebug
  baseOutputDir = layout.buildDirectory.dir("build-results")

  // devices to test on, Defaults to [[model: 'Pixel2', version: 27]]
  devices = [
    [model: 'NexusLowRes', version: 30, atd: true],
    [model: 'Pixel2', version: 23]
  ]

  // whether to enable Android orchestrator, if your app has orchestrator
  // configured this will get picked up automatically, however you can
  // force-change the value here if you want to
  useOrchestrator = true
  
  // whether to clear package data before running each test (orchestrator only)
  // if your app has this configured via testInstrumentationRunnerArguments then
  // it will get picked up automatically
  clearPackageData = true
  
  // if true, the Gradle plugin will fetch coverage data and store under
  // `baseOutputDir/${variant}`, if your app has coverage enabled this will be
  // enabled automatically
  withCoverage = true

  // additional APKs to install, you can pass in `project.files(...)` or a 
  // Gradle configuration here
  additionalApks = configurations.additionalTestApks
  
  // additional arguments to AndroidJUnitRunner, by default emulator.wtf Gradle
  // plugin will pick these up from testInstrumentationRunnerArguments, however
  // you can override (or unset with null) these values here
  // 
  // for instance to only run medium tests:
  environmentVariables = [size: 'medium']

  // Set to a number larger than 1 to randomly split your tests into multiple
  // shards to be executed in parallel
  numUniformShards = 3
  
  // Set to a number larger than 1 to split your tests into multiple shards
  // based on test counts to be executed in parallel
  numShards = 3

  // Directories to pull from device after test is over, will be stored in
  // baseOutputDir/${variant}:
  diretoriesToPull = ['/sdcard/screenshots']
}
```

## Common examples

### Run tests with multiple device profiles

By default emulator.wtf runs tests on a Pixel2-like emulator with API 27
(Android 8.1). If you want to run on a different version or device profile you
can specify devices like so:

```groovy
emulatorwtf {
  devices = [
    [model: "NexusLowRes", version: 23],
    [model: "Pixel2", version: 27]
  ]
}
```

### Run tests with shards

The following example runs tests in parallel using 3 separate shards and stores
the outputs from each shard in a separate folder under `app/build/test-results`:

```groovy
emulatorwtf {
  numshards = 3
}
```
