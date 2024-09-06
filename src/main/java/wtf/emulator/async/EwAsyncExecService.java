package wtf.emulator.async;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.process.ExecOperations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.emulator.EwPlugin;
import wtf.emulator.data.CliOutputAsync;
import wtf.emulator.exec.EwCliExecutor;
import wtf.emulator.exec.EwCliOutput;
import wtf.emulator.exec.EwCollectResultsWorkParameters;
import wtf.emulator.exec.EwWorkParameters;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class EwAsyncExecService implements BuildService<BuildServiceParameters.None>, AutoCloseable {
  public static final String NAME = "ewAsyncExecService";

  private static final Logger log = LoggerFactory.getLogger(EwAsyncExecService.class);

  private final ExecutorService threads = Executors.newCachedThreadPool();

  private final AtomicInteger reqsInFlight = new AtomicInteger(0);

  private final List<CompletableFuture<AsyncRunData>> futures = new CopyOnWriteArrayList<>();

  @Inject
  public abstract ExecOperations getExecOperations();

  public void executeAsync(EwWorkParameters parameters) {
    log.info("Execute asynchronously: {}", parameters.getDisplayName().get());

    EwCliExecutor cliExecutor = new EwCliExecutor(EwPlugin.gson, getExecOperations());

    reqsInFlight.incrementAndGet();
    futures.add(CompletableFuture.supplyAsync(() -> {
      try {
        CliOutputAsync out = cliExecutor.invokeCli(parameters).async();

        if (out == null) {
          throw new RuntimeException("No async output, this looks like a bug in the wtf.emulator.gradle plugin? Let us know at support@emulator.wtf!");
        }

        String displayName = parameters.getDisplayName().get();
        return new AsyncRunData(out.runUuid(), out.runToken(), out.startTime(), displayName);
      }
      finally {
        reqsInFlight.decrementAndGet();
      }
    }, threads));
  }

  public List<String> drainResults(EwCollectResultsWorkParameters parameters) {
    EwCliExecutor cliExecutor = new EwCliExecutor(EwPlugin.gson, getExecOperations());

    //noinspection unchecked
    CompletableFuture<String>[] messageFutures = futures.stream().map((future) ->
        future.thenApply((data) -> {
          // sanitize folder name
          String folderName = data.getDisplayName().replace(':', '_')
              .replaceAll("[^a-zA-Z0-9_]", "_")
              .replace("^_*", "");
          try {
            return cliExecutor.collectRunResults(parameters, data.getRunUuid(), data.getRunToken(), data.getStartTime(), data.getDisplayName(), folderName);
          }
          catch (Exception e) { /* eat */ }
          return "";
        })
    ).toArray(CompletableFuture[]::new);
    CompletableFuture<Void> drain = CompletableFuture.allOf(messageFutures);

    // wait up to 10 minutes for all results to be collected
    try {
      drain.get(10, TimeUnit.MINUTES);
    } catch (InterruptedException | TimeoutException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      /* ignore */
    }

    return Arrays.stream(messageFutures).map(future -> future.isCompletedExceptionally() ? "" : future.getNow("")).collect(Collectors.toList());
  }

  @Override
  public void close() {
    try {
      threads.shutdown();
      if (reqsInFlight.get() == 0) {
        return;
      }

      // use stdout here because Gradle will silence loggers after the build is complete and it is closing build services
      System.out.println("Waiting up to 5 minutes for " + reqsInFlight.get() + " emulator.wtf tests to trigger before exiting");

      try {
        long start = System.nanoTime();
        boolean timedOut = threads.awaitTermination(5, TimeUnit.MINUTES);
        if (!timedOut) {
          System.out.println("Timed out while waiting for emulator.wtf tests to trigger");
        }
        else {
          long elapsed = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
          System.out.println("All emulator.wtf tests triggered after waiting " + elapsed + " seconds");
        }
      }
      catch (InterruptedException e) {
        System.out.println("Interrupted while waiting for emulator.wtf tests to trigger");
      }
    } finally {
      futures.clear();
    }
  }
}
