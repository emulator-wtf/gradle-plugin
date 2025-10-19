package wtf.emulator;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.time.Duration;

public interface EwInvokeConfiguration {
  /**
   * Specify the list of various output types (e.g. {@link OutputType#LOGCAT}, {@link OutputType#MERGED_RESULTS_XML}, etc)
   * to download after the test has finished.
   */
  ListProperty<OutputType> getOutputs();

  /**
   * Whether to record video of the test run. Defaults to true.
   */
  Property<Boolean> getRecordVideo();

  /**
   * Whether to use the orchestrator test runner.
   * See more about orchestrator <a href="https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#use-android">here</a>
   */
  Property<Boolean> getUseOrchestrator();

  /**
   * Set to true to clear app data (shared prefs, databases, files) before each test run.
   * Requires {@link #getUseOrchestrator()} to be set to true.
   */
  Property<Boolean> getClearPackageData();

  /**
   * Set to true to fetch coverage data from the emulator after the test run has finished.
   */
  Property<Boolean> getWithCoverage();

  /**
   * Specify the list of additional APKs to install before running the tests, for
   * more complex multi-app end-to-end tests.
   */
  Property<FileCollection> getAdditionalApks();

  /**
   * Use {@code AndroidJUnitRunner}'s {@code numShards} and {@code shardIndex} environment variables to spread
   * tests roughly uniformly across shards.
   * Not recommended for use, prefer {@link #getShardTargetRuntime()} or {@link #getNumBalancedShards()} instead.
   * Read more about uniform sharding <a href="https://docs.emulator.wtf/concepts/sharding/#uniform-sharding">here</a>.
   */
  Property<Integer> getNumUniformShards();

  /**
   * Evenly splits tests to shards based on the counts. For example with a test suite of 40 tests and 4 shards, each
   * shard will use exactly 10 tests.
   * Read more about even sharding <a href="https://docs.emulator.wtf/concepts/sharding/#even-sharding">here</a>.
   */
  Property<Integer> getNumShards();

  /**
   * Split tests to shards based on the runtime of each test, with the goal of making each shard run for roughly the
   * same amount of time.
   * Read more about balanced sharding <a href="https://docs.emulator.wtf/concepts/sharding/#balanced-sharding">here</a>.
   */
  Property<Integer> getNumBalancedShards();

  /**
   * Set the target runtime for each shard in seconds. This will be used to calculate the number of shards to use.
   * The most hands-off approach to sharding - just set the target runtime and let emulator.wtf do the rest.
   * Read more about targeted runtime sharding <a href="https://docs.emulator.wtf/concepts/sharding/#targeted-runtime-sharding">here</a>.
   */
  Property<Integer> getShardTargetRuntime();

  /**
   * Set the list of directories to pull from the emulator after the test run has finished.
   * NOTE: make sure to include {@link OutputType#PULLED_DIRS} in {@link #getOutputs()} to download the pulled directories
   * if needed.
   */
  ListProperty<String> getDirectoriesToPull();

  /**
   * Indicates that the test run has side effects, i.e. it hits external resources and might be a part of a bigger test
   * suite. Adding this flag means that the test will not be automatically retried in case of errors and any Gradle
   * caching for the test tasks will be disabled.
   */
  Property<Boolean> getSideEffects();

  /**
   * Fail if the test runtime exceeds the given timeout value.
   */
  Property<Duration> getTimeout();

  /**
   * Enable-disable the file cache for the test run. The file cache will store the input test files (apks) so repeated
   * test runs with the same input apks can skip uploading them. You can control how long the files are stored in the
   * cache with the {@link #getFileCacheTtl()} property.
   */
  Property<Boolean> getFileCacheEnabled();

  /**
   * Set the time-to-live for the files stored in the file cache. The files will be deleted after the given duration.
   * Only relevant if {@link #getFileCacheEnabled()} is set to true.
   */
  Property<Duration> getFileCacheTtl();

  /**
   * Enable-disable the test cache for the test run. The test cache will store the test results so repeated test runs
   * with the same input apks and configuration can skip running the tests.
   */
  Property<Boolean> getTestCacheEnabled();

  /**
   * Add repeat attempts of devices and/or shards where there were test failures. Maximum number of flaky test attempts
   * is 10. The test attempts will be started in parallel, e.g. with number of flaky test attempts of 3 an extra three
   * attempts will be started in case of a test failure.
   */
  Property<Integer> getNumFlakyTestAttempts();

  /**
   * Whether to repeat the whole failed shard ({@link FlakyRepeatMode#ALL}) or only the failed tests
   * ({@link FlakyRepeatMode#FAILED_ONLY}) in case of flaky tests.
   * The default mode is {@link FlakyRepeatMode#FAILED_ONLY}.
   */
  Property<FlakyRepeatMode> getFlakyTestRepeatMode();

  /**
   * Display name of the test run in the emulator.wtf web app results UI.
   */
  Property<String> getDisplayName();

  /**
   * List of IPv4 DNS servers to use for the emulator. Can be used together with
   * {@link #getEgressTunnel()} to use DNS servers otherwise not visible to the emulator.
   *
   * If not set, the emulator will use the default DNS servers configured in the system.
   */
  ListProperty<String> getDnsServers();

  /**
   * Set to true to redirect all network traffic from the emulator instance to the host running Gradle
   * as if you were running the emulator locally. You can use this to test your app with a local server
   * or an internal environment only accessible to your local machine or CI runner.
   * Read more about the egress tunnel <a href="https://docs.emulator.wtf/concepts/networking/#egress-tunnel">here</a>.
   */
  Property<Boolean> getEgressTunnel();

  /**
   * Override DNS resolution of specific hostnames to provided IP addresses, a mechanism similar to the
   * /etc/hosts file on UNIX operating systems.
   */
  ListProperty<DnsOverride> getDnsOverrides();

  /**
   * Limit which relay servers to use for egress tunnel and adb connection.
   * NOTE: has performance implications, use only when necessary.
   */
  ListProperty<String> getRelays();

  /**
   * Makes the host running Gradle available to the emulator instance under the specified IP address when
   * using the egress tunnel.
   * This is useful for testing local servers.
   * NOTE: this should NOT be a public IP, loopback IP or a broadcast IP address.
   */
  Property<String> getEgressLocalhostForwardIp();

  /**
   * Explicitly set the URL of the source control management system where the test run was triggered from.
   * If not set this will be guessed based on working directory git information.
   */
  Property<String> getScmUrl();

  /**
   * Explicitly set the commit hash of the source control management system where the test run was triggered from.
   * If not set this will be guessed based on working directory git information.
   */
  Property<String> getScmCommitHash();

  /**
   * Explicitly set the branch name of the source control management system where the test run was triggered from.
   * If not set this will be guessed based on working directory git information.
   */
  Property<String> getScmRefName();

  /**
   * Explicitly set the pull request URL of the source control management system where the test run was triggered from.
   * If not set this will be guessed based on working directory git information.
   */
  Property<String> getScmPrUrl();

  /**
   * Set to true to not fail the tasks/build if tests are failing.
   * The tasks will still fail if any other error occurs (timeouts, emulator.wtf failures, etc).
   */
  Property<Boolean> getIgnoreFailures();

  /**
   * Run the test asynchronously, without waiting for the results. This shines when used together with our
   * <a href="https://docs.emulator.wtf/github/commit-statuses/">GitHub integration</a>.
   */
  Property<Boolean> getAsync();

  /**
   * Set to true to print any {@code ew-cli} raw output to the console while running the tests.
   */
  Property<Boolean> getPrintOutput();

  /**
   * Run only a subset of matching test targets, these will be forwarded to {@code AndroidJUnitRunner}.
   * See the full list of configuration options
   * <a href="https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#typical-usage">here</a>.
   */
  Property<TestTargetsSpec> getTestTargets();

  /**
   * Explicitly set the proxy host to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyHost();

  /**
   * Explicitly set the proxy port to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<Integer> getProxyPort();

  /**
   * Explicitly set the proxy username to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyUser();

  /**
   * Explicitly set the proxy password to use for communicating with emulator.wtf backend.
   * By default the Java system properties are used for discovering proxy settings.
   */
  Property<String> getProxyPassword();
}
