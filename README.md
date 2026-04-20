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

In addition to the variant specific tasks there will be an anchor task named
`testWithEmulatorWtf`, it'll depend on _all_ the variant-specific tasks. This allows
you to run all tests for all subprojects with a single `./gradlew testWithEmulatorWtf`
invoke. See the configuration section below on disabling the task for some variants.

You can always run `./gradlew :app:tasks` to see the added tasks, they will be
listed under the _Verification_ section.

## Installation

### Option 1: Using plugins DSL (`plugins {}`)

Make sure `mavenCentral()` repository is in your `settings.gradle(.kts)` file:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

And then you can enable the plugin by adding it to the `plugins` block of your
app project (usually under `app/build.gradle(.kts)`):

<details open>
<summary>Kotlin DSL</summary>

```kotlin
plugins {
    id("wtf.emulator.gradle") version "1.5.3"
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
plugins {
    id "wtf.emulator.gradle" version "1.5.3"
}
```

</details>

### Option 2: Using `buildscript` classpath (`apply plugin`)

Add the emulator.wtf plugin to your `buildscript` classpath in the root
`build.gradle(.kts)` file:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // ... other deps here, like com.android.tools.build:gradle
        classpath("wtf.emulator:gradle-plugin:1.5.3")
    }
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // ... other deps here, like com.android.tools.build:gradle
        classpath "wtf.emulator:gradle-plugin:1.5.3"
    }
}
```

</details>

You can then apply the plugin in your app project (usually in `app/build.gradle(.kts)`):

<details open>
<summary>Kotlin DSL</summary>

```kotlin
apply(plugin = "wtf.emulator.gradle")
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
apply plugin: 'wtf.emulator.gradle'
```

</details>

### Add the emulator.wtf Maven repository

The plugin resolves the `ew-cli` runner from the emulator.wtf Maven repository at runtime. Add the following to your `settings.gradle(.kts)` file to make it available for dependency resolution.

<details open>
<summary>Kotlin DSL</summary>

```kotlin
dependencyResolutionManagement {
    repositories {
        // ... other repos here, like mavenCentral(), google(), etc

        maven(url = "https://maven.emulator.wtf/releases/") {
            content { includeGroup("wtf.emulator") }
        }
    }
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
dependencyResolutionManagement {
    repositories {
        // ... other repos here, like mavenCentral(), google(), etc

        maven {
            url = "https://maven.emulator.wtf/releases/"
            content { includeGroup("wtf.emulator") }
        }
    }
}
```

</details>

### Token

To run tests you'll need to pass your API token to the Gradle plugin. The
recommended way to do so is via defining a `EW_API_TOKEN` environment variable,
it'll get picked up by the plugin automatically. This way you don't have to
worry about passing the token to your Gradle build.

Alternatively, you can use the `token` option on the `emulatorwtf` extension.
For example, to grab the token via a `ewApiToken` project property:

```kotlin
emulatorwtf {
    token.set(project.providers.gradleProperty("ewApiToken"))
}
```

> [!NOTE]
> Hardcoding API tokens in your `build.gradle` files is not recommended.

## Configuration

The `emulatorwtf` plugin DSL supports the following configuration options:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
import wtf.emulator.DeviceModel
import wtf.emulator.DnsOverride
import wtf.emulator.FlakyRepeatMode
import wtf.emulator.GpuMode
import wtf.emulator.OutputType
import wtf.emulator.ShardUnit
import wtf.emulator.TestReporter
import java.time.Duration

emulatorwtf {
    // CLI version to use, defaults to 1.2.2
    version.set("1.2.2")

    // emulator.wtf API token, we recommend either using the EW_API_TOKEN env var
    // instead of this or passing this value in via a project property
    token.set("AQAA...")

    // where to store results in, they will be further scoped by the variant name,
    // i.e. ./gradlew :app:testFreeDebugWithEmulatorWtf will store outputs in
    // build/build-results/freeDebug
    baseOutputDir.set(layout.buildDirectory.dir("build-results"))

    // Specify what kind of outputs to store in the base output dir
    // default: [OutputType.MERGED_RESULTS_XML, OutputType.COVERAGE, OutputType.PULLED_DIRS]
    outputs.set(listOf(OutputType.SUMMARY, OutputType.CAPTURED_VIDEO, OutputType.LOGCAT))

    // Always print the ew-cli output the console when running. Useful for debugging.
    printOutput.set(true)

    // record a video of the test run
    recordVideo.set(true)

    // ignore test failures and keep running the build, defaults to false
    //
    // NOTE: the build outcome _will_ be success at the end, use the JUnit XML files to
    //       check for test failures
    ignoreFailures.set(false)

    // devices to test on, Defaults to Pixel7, version 30, gpu auto
    device {
        model.set(DeviceModel.PIXEL_7)
        version.set(30)
        gpu.set(GpuMode.AUTO)
    }
    device {
        model.set(DeviceModel.NEXUS_LOW_RES)
        version.set(21)
        locale.set("fr_CA")
        gpu.set(GpuMode.SOFTWARE)
    }

    // Set the test timeout, defaults to 15 minutes
    timeout.set(Duration.ofHours(1))

    // whether to enable Android orchestrator, if your app has orchestrator
    // configured this will get picked up automatically, however you can
    // force-change the value here if you want to
    useOrchestrator.set(true)

    // whether to clear package data before running each test (orchestrator only)
    // if your app has this configured via testInstrumentationRunnerArguments then
    // it will get picked up automatically
    clearPackageData.set(true)

    // if true, the Gradle plugin will fetch coverage data and store under
    // `baseOutputDir/${variant}`, if your app has coverage enabled this will be
    // enabled automatically
    withCoverage.set(true)

    // additional APKs to install, you can pass in `project.files(...)` or a 
    // Gradle configuration here
    additionalApks.set(project.configurations["additionalTestApks"])

    // additional arguments to AndroidJUnitRunner, by default emulator.wtf Gradle
    // plugin will pick these up from testInstrumentationRunnerArguments, however
    // you can override (or unset with null) these values here
    // 
    // for instance to only run medium tests:
    environmentVariables.set(mapOf("size" to "medium"))

    // additional arguments to AndroidJUnitRunner, similar to the environmentVariables
    // above, but in this case the arguments will be hidden in the emulator.wtf UI.
    // Use this for passing any sort of secrets - tokens, passwords, credentials, etc.
    secretEnvironmentVariables.set(mapOf("token" to "hunter2"))

    // Set to the a minutes value to split your tests into multiple shards
    // dynamically, the number of shards will be figured out based on historical
    // test times. This is a good way to ensure a consistent runtime as your
    // testsuite grows or shrinks - we will adjust the number of shards as
    // needed
    shardTargetRuntime.set(2)

    // Set to a number larger than 1 to randomly split your tests into multiple
    // shards to be executed in parallel
    numUniformShards.set(3)

    // Set to a number larger than 1 to split your tests into multiple shards
    // based on test counts to be executed in parallel
    numShards.set(3)

    // Set to a number larger than 1 to split your tests into multiple shards
    // based on historic test time to be executed in parallel
    numBalancedShards.set(3)

    // Hint the average runtime for a test case in the test suite. This will be used
    // by the sharding algorithm when no historical data is available and shardTargetRuntime
    // or numBalancedShards is used. Defaults to 10 seconds.
    testcaseDurationHint.set(Duration.ofSeconds(10))

    // Set the granularity level for sharding. Can be ShardUnit.TEST_CLASSES to shard 
    // at the test class level, or ShardUnit.TEST_METHODS (default) to shard at the 
    // test method level.
    shardUnit.set(ShardUnit.TEST_CLASSES)

    // Set to a non-zero value to repeat device/shards that failed, the repeat
    // attempts will be executed in parallel
    numFlakyTestAttempts.set(3)

    // Whether to reattempt full shards (ALL) or only failed tests (FAILED_ONLY)
    // in case of test failures. Defaults to FAILED_ONLY.
    flakyTestRepeatMode.set(FlakyRepeatMode.FAILED_ONLY)

    // Directories to pull from device after test is over, will be stored in
    // baseOutputDir/${variant}:
    directoriesToPull.set(listOf("/sdcard/screenshots"))

    // Enable-disable the test input file cache (APKs etc)
    fileCacheEnabled.set(false)

    // Set the maximum time-to-live of items in the test input file cache
    fileCacheTtl.set(Duration.ofHours(3))

    // Disable caching test results in the backend
    // NOTE! This will not disable caching at the Gradle task or Gradle build cache level,
    // use sideEffects.set(true to disable all caching
    testCacheEnabled.set(false)

    // Continue after triggering the tests. No outputs will be saved.
    async.set(true)

    // Manually set the displayName of the tests. Defaults to the module path + variant name (if there
    // are multiple testable variants)
    displayName.set("instrumentation tests")

    // Filter to specific test targets to run, these will be forwarded to the 'am instrument ...' command
    // Read more at https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#typical-usage
    // default: all tests will be run
    targets {
        testClass("foo.bar.Baz")
    }

    // Do not generate the test task for some specific variants
    variantFilter {
        if (variant.buildType == "release") {
            isEnabled = false
        }
    }

    // Use a specific DNS server instead of the default one.
    dnsServers.set(listOf("1.1.1.1"))

    // Redirects all network traffic from the emulator instance to the Gradle plugin
    // as if you were running the emulator locally.
    // You can use this to test your app with a local server or an internal
    // environment only accessible to your local machine or CI runner.
    egressTunnel.set(false)

    // Hard-code specific hostname-ip combinations.
    dnsOverrides.set(listOf(
        DnsOverride.create("example.com", "127.0.0.1")
    ))

    // Makes the machine the Gradle build is running on visible to the emulator under the given ipv4 address,
    // only works together with the egressTunnel option
    egressLocalhostForwardIp.set("192.168.200.1")

    // Configure a HTTP proxy to use when making requests to emulator.wtf API
    // these values default to standard JVM system properties `http.proxyHost`,
    // `http.proxyPort`, `http.proxyUser` and `http.proxyPassword` - there's no need to specify
    // them if your Gradle daemon has these props set.
    // NOTE: this is for setting up the test, it has no effect on your tests in the emulator
    proxyHost.set("localhost")
    proxyPort.set(8080)
    proxyUser.set("user")
    proxyPassword.set("hunter2")

    // Configure the test reporters to use.
    // GRADLE_TEST_REPORTING_API - test results will be reported via Gradle Test Reporting API (Gradle 8.13+)
    // and will show up in console summaries, HTML reports, and build scans.
    // DEVELOCITY - test results will be reported via Develocity JUnit importer API.
    // By default no reporters are enabled.
    testReporters.set(listOf(TestReporter.DEVELOCITY, TestReporter.GRADLE_TEST_REPORTING_API))
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
import wtf.emulator.DeviceModel
import wtf.emulator.DnsOverride
import wtf.emulator.FlakyRepeatMode
import wtf.emulator.GpuMode
import wtf.emulator.OutputType
import wtf.emulator.ShardUnit
import wtf.emulator.TestReporter
import java.time.Duration

emulatorwtf {
    // CLI version to use, defaults to 1.2.2
    version = '1.2.2'

    // emulator.wtf API token, we recommend either using the EW_API_TOKEN env var
    // instead of this or passing this value in via a project property
    token = 'AQAA...'

    // where to store results in, they will be further scoped by the variant name,
    // i.e. ./gradlew :app:testFreeDebugWithEmulatorWtf will store outputs in
    // build/build-results/freeDebug
    baseOutputDir = layout.buildDirectory.dir("build-results")

    // Specify what kind of outputs to store in the base output dir
    // default: [OutputType.MERGED_RESULTS_XML, OutputType.COVERAGE, OutputType.PULLED_DIRS]
    outputs = [OutputType.SUMMARY, OutputType.CAPTURED_VIDEO, OutputType.LOGCAT]

    // Always print the ew-cli output the console when running. Useful for debugging.
    printOutput = true

    // record a video of the test run
    recordVideo = true

    // ignore test failures and keep running the build, defaults to false
    //
    // NOTE: the build outcome _will_ be success at the end, use the JUnit XML files to
    //       check for test failures
    ignoreFailures = false

    // devices to test on, Defaults to Pixel7, version 30, gpu auto
    device {
        model = DeviceModel.PIXEL_7
        version = 30
        gpu = GpuMode.AUTO
    }
    device {
        model = DeviceModel.NEXUS_LOW_RES
        version = 21
        locale = "fr_CA"
        gpu = GpuMode.SOFTWARE
    }

    // Set the test timeout, defaults to 15 minutes
    timeout = Duration.ofHours(1)

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
    additionalApks = project.configurations.additionalTestApks

    // additional arguments to AndroidJUnitRunner, by default emulator.wtf Gradle
    // plugin will pick these up from testInstrumentationRunnerArguments, however
    // you can override (or unset with null) these values here
    // 
    // for instance to only run medium tests:
    environmentVariables = [size: 'medium']

    // additional arguments to AndroidJUnitRunner, similar to the environmentVariables
    // above, but in this case the arguments will be hidden in the emulator.wtf UI.
    // Use this for passing any sort of secrets - tokens, passwords, credentials, etc.
    secretEnvironmentVariables = [token: 'hunter2']

    // Set to the a minutes value to split your tests into multiple shards
    // dynamically, the number of shards will be figured out based on historical
    // test times. This is a good way to ensure a consistent runtime as your
    // testsuite grows or shrinks - we will adjust the number of shards as
    // needed
    shardTargetRuntime = 2

    // Set to a number larger than 1 to randomly split your tests into multiple
    // shards to be executed in parallel
    numUniformShards = 3

    // Set to a number larger than 1 to split your tests into multiple shards
    // based on test counts to be executed in parallel
    numShards = 3

    // Set to a number larger than 1 to split your tests into multiple shards
    // based on historic test time to be executed in parallel
    numBalancedShards = 3

    // Hint the average runtime for a test case in the test suite. This will be used
    // by the sharding algorithm when no historical data is available and shardTargetRuntime
    // or numBalancedShards is used. Defaults to 10 seconds.
    testcaseDurationHint = Duration.ofSeconds(10)

    // Set the granularity level for sharding. Can be ShardUnit.TEST_CLASSES to shard 
    // at the test class level, or ShardUnit.TEST_METHODS (default) to shard at the 
    // test method level.
    shardUnit = ShardUnit.TEST_CLASSES

    // Set to a non-zero value to repeat device/shards that failed, the repeat
    // attempts will be executed in parallel
    numFlakyTestAttempts = 3

    // Whether to reattempt full shards (ALL) or only failed tests (FAILED_ONLY)
    // in case of test failures. Defaults to FAILED_ONLY.
    flakyTestRepeatMode = FlakyRepeatMode.FAILED_ONLY

    // Directories to pull from device after test is over, will be stored in
    // baseOutputDir/${variant}:
    diretoriesToPull = ['/sdcard/screenshots']

    // Enable-disable the test input file cache (APKs etc)
    fileCacheEnabled = false

    // Set the maximum time-to-live of items in the test input file cache
    fileCacheTtl = Duration.ofHours(3)

    // Disable caching test results in the backend
    // NOTE! This will not disable caching at the Gradle task or Gradle build cache level,
    // use sideEffects = true to disable all caching
    testCacheEnabled = false

    // Continue after triggering the tests. No outputs will be saved.
    async = true

    // Manually set the displayName of the tests. Defaults to the module path + variant name (if there
    // are multiple testable variants)
    displayName = "instrumentation tests"

    // Filter to specific test targets to run, these will be forwarded to the 'am instrument ...' command
    // Read more at https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#typical-usage
    // default: all tests will be run
    targets {
        testClass("foo.bar.Baz")
    }

    // Do not generate the test task for some specific variants
    variantFilter {
        if (variant.buildType == 'release') {
            enabled = false
        }
    }

    // Use a specific DNS server instead of the default one.
    dnsServers = ["1.1.1.1"]

    // Redirects all network traffic from the emulator instance to the Gradle plugin
    // as if you were running the emulator locally.
    // You can use this to test your app with a local server or an internal
    // environment only accessible to your local machine or CI runner.
    egressTunnel = false

    // Hard-code specific hostname-ip combinations.
    dnsOverrides = [
            DnsOverride.create("example.com", "127.0.0.1")
    ]

    // Makes the machine the Gradle build is running on visible to the emulator under the given ipv4 address,
    // only works together with the egressTunnel option
    egressLocalhostForwardIp = "192.168.200.1"

    // Configure a HTTP proxy to use when making requests to emulator.wtf API
    // these values default to standard JVM system properties `http.proxyHost`,
    // `http.proxyPort`, `http.proxyUser` and `http.proxyPassword` - there's no need to specify
    // them if your Gradle daemon has these props set.
    // NOTE: this is for setting up the test, it has no effect on your tests in the emulator
    proxyHost = "localhost"
    proxyPort = 8080
    proxyUser = "user"
    proxyPassword = "hunter2"

    // Configure the test reporters to use.
    // GRADLE_TEST_REPORTING_API - test results will be reported via Gradle Test Reporting API (Gradle 8.13+)
    // and will show up in console summaries, HTML reports, and build scans.
    // DEVELOCITY - test results will be reported via Develocity JUnit importer API.
    // By default no reporters are enabled.
    testReporters = [TestReporter.DEVELOCITY, TestReporter.GRADLE_TEST_REPORTING_API]
}
```

</details>

## Common examples

### Run tests with multiple device profiles

By default, emulator.wtf runs tests on a Pixel7-like emulator with API 30
(Android 11). If you want to run on a different version or device profile you
can specify devices like so:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
emulatorwtf {
    device {
        model.set(DeviceModel.NEXUS_LOW_RES)
        version.set(23)
    }
    device {
        model.set(DeviceModel.PIXEL_2)
        version.set(27)
    }
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
emulatorwtf {
    device {
        model = DeviceModel.NEXUS_LOW_RES
        version = 23
    }
    device {
        model = DeviceModel.PIXEL_2
        version = 27
    }
}
```

</details>

### Run tests with shards

The following example runs tests in parallel using 3 separate shards and stores
the outputs from each shard in a separate folder under `app/build/test-results`:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
emulatorwtf {
    numShards.set(3)
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
emulatorwtf {
    numShards = 3
}
```

</details>

### Use Gradle-managed devices

The plugin supports configuring your devices via [Gradle-managed devices](https://developer.android.com/studio/test/gradle-managed-devices).

1) Enable custom devices in `gradle.properties` when using AGP version 8.2 or lower:
    ```properties
    android.experimental.testOptions.managedDevices.customDevice=true
    ```
2) Configure the device(s) in your module level build.gradle file:

    <details open>
    <summary>Kotlin DSL</summary>

    ```kotlin
    import wtf.emulator.ewDevices
    import wtf.emulator.DeviceModel
    
    android {
        testOptions {
            managedDevices { 
                ewDevices {
                    register("ewPixel7api33") { 
                        device.set(DeviceModel.PIXEL_7)
                        apiLevel.set(33)
                    }
                }
            }
        }
    }
    ```
    </details>

    <details>
    <summary>Groovy DSL</summary>

    ```groovy
    import wtf.emulator.gmd.EwManagedDevice
    import wtf.emulator.DeviceModel
    
    android {
        testOptions {
            managedDevices {
                allDevices {
                    register("ewPixel7api33", EwManagedDevice) {
                        device = DeviceModel.PIXEL_7
                        apiLevel = 33
                    }
                }
            }
        }
    }
    ```
    </details>
3) Optional: configure relevant `emulatorwtf {}` options in your module level `build.gradle(.kts)` file as described above in previous examples.

To use these devices to run your tests, run the following Gradle task: `{deviceName}{BuildVariant}AndroidTest`. For example:
```bash
./gradlew ewPixel7api33DebugAndroidTest
```

#### Creating baseling profiles with Gradle-managed devices

You can set up baselines profiles in the [same way you would do with local emulators](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile) and configure the device(s) that you want to run on via GMD definitions shown in the example above.

Your final configuration could look like this:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
import wtf.emulator.ewDevices
import wtf.emulator.DeviceModel

android {
    testOptions.managedDevices.ewDevices {
        register("ewPixel7api33") {
            device = DeviceModel.PIXEL_7
            apiLevel = 33
        }
    }
}

baselineProfile {
    managedDevices += "ewPixel7api33"
    useConnectedDevices = false
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
import wtf.emulator.gmd.EwManagedDevice
import wtf.emulator.DeviceModel

android {
    testOptions.managedDevices.allDevices {
        register("ewPixel7api33", EwManagedDevice) {
            device = DeviceModel.PIXEL_7
            apiLevel = 33
        }
    }
}

baselineProfile {
    managedDevices += "ewPixel7api33"
    useConnectedDevices = false
}
```

</details>

### Create multiple test run configurations

You can create multiple test run configurations with different device and test target settings.
This allows you to run different subsets of tests on different devices, optimizing your test execution based on your needs.

The configurations derive from the base default configuration specified in the `emulatorwtf {}` block.

Here's a snippet that defines multiple test run configurations, a default one, a smoke test suite for quick
validation and a longer end-to-end test suite:

<details open>
<summary>Kotlin DSL</summary>

```kotlin
emulatorwtf {
    // default to two devices
    device {
        model.set(DeviceModel.PIXEL_7)
        version.set(35)
    }
    device {
        model.set(DeviceModel.PIXEL_7)
        version.set(24)
    }

    // don't run any tests annotated by E2eTest by default
    targets {
        excludeAnnotation("com.example.E2eTest")
    }

    configurations {
        // smoke test suite
        create("smoke") {
            // only run the SmokeTest target
            targets {
                annotation("com.example.SmokeTest")
            }
            // run smoke tests only on 35
            device {
                model.set(DeviceModel.PIXEL_7)
                version.set(35)
            }
        }
        // end-to-end test suite, runs on both devices configured above
        create("e2e") {
            // only run E2E tests
            targets {
                annotation("com.example.E2eTest")
            }
        }
    }
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
emulatorwtf {
    // default to two devices
    device {
        model = DeviceModel.PIXEL_7
        version = 35
    }
    device {
        model = DeviceModel.PIXEL_7
        version = 24
    }

    // don't run any tests annotated by E2eTest by default
    targets {
        excludeAnnotation("com.example.E2eTest")
    }

    configurations {
        // smoke test suite
        create("smoke") {
            // only run the SmokeTest target
            targets {
                annotation("com.example.SmokeTest")
            }
            // run smoke tests only on 35
            device {
                model = DeviceModel.PIXEL_7
                version = 35
            }
        }
        // end-to-end test suite, runs on both devices configured above
        create("e2e") {
            // only run E2E tests
            targets {
                annotation("com.example.E2eTest")
            }
        }
    }
}
```

</details>

This will create 3 separate `test*WithEmulatorWtf` tasks:

- `:app:testDebugWithEmulatorWtf` - runs on 2 api versions and ignores anything with the `@E2eTest` annotation
- `:app:testSmokeDebugWithEmulatorWtf` - runs on a single api version and only includes tests with the `@SmokeTest` annotation
- `:app:testE2eDebugWithEmulatorWtf` - runs on 2 api versions and only includes tests with the `@E2eTest` annotation

## Compatibility

The plugin is compatible with any working combination of these ranges:

| Component             | Oldest | Newest        |
|-----------------------|--------|---------------|
| JDK                   | 17     | 26            |
| Gradle                | 8.0    | 9.5.0-rc-3    |
| Android Gradle Plugin | 8.1.0  | 9.3.0-alpha01 |

> [!NOTE]
> Only the latest of any prerelease versions (`alpha`, `beta`, `rc`) is supported.
