/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.core.position.PositionSource;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class ServerRunner {

  private ServerRunner() {
  }

  public static void main(String[] args) throws Exception {
    ServerRunner.run();
  }

  public static void run() throws IOException, InterruptedException {
    // TODO is it possible to scan the view defs and run all of them?
    // need to be able to choose the snapshot for each view def. if there's only one for each base view def then
    // it should be possible
    // TODO how does the web resource figure out the base view for a snapshot?
    String projectName = "examples-simulated";
    String version = "2.2.0-SNAPSHOT";
    String configFile = "classpath:fullstack/fullstack-examplessimulated-bin.properties";
    String serverJar = projectName + "-" + version + ".jar";
    String classpath = "config:lib/" + serverJar;
    String logbackConfig = "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml";
    String viewDefName = "AUD Swaps (3m / 6m basis) (1)";
    String snapshotTime = "2013-09-27T12:17:45.587Z";
    String snapshotName = viewDefName + "/" + snapshotTime;
    int serverHttpPort = 8080;
    Instant valuationTime = Instant.now();

    // after 'mvn install' the server is in
    // $PROJECT_DIR/server/target/server-dir
    // TODO ATM this is running from the same location but that won't be the case forever
    // do I want to switch the branch underneath the running installation? seems dodgy but probably OK for testing
    ProcessBuilder processBuilder = new ProcessBuilder("java",
                                                       logbackConfig, // TODO why isn't this getting picked up?
                                                       "-cp",
                                                       classpath,
                                                       "-Xmx2g",
                                                       "-XX:MaxPermSize=256M",
                                                       "com.opengamma.component.OpenGammaComponentServer",
                                                       configFile);
    Process process = processBuilder.start();
    BlockingQueue<Boolean> startupQueue = new ArrayBlockingQueue<>(1);
    consumeStream(process.getInputStream(), OpenGammaComponentServer.STARTUP_COMPLETE_MESSAGE, startupQueue, true, System.out);
    consumeStream(process.getErrorStream(), OpenGammaComponentServer.STARTUP_FAILED_MESSAGE, startupQueue, false, System.err);
    Boolean startupSuccess = startupQueue.take();

    if (!startupSuccess) {
      System.out.println("startup failed, aborting");
      return;
    }
    try (RemoteServer server = RemoteServer.create("http://localhost:" + serverHttpPort)) {
      // look up the snapshot by name to get the ID
      MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
      snapshotSearchRequest.setName(snapshotName);
      snapshotSearchRequest.setIncludeData(false);
      MarketDataSnapshotSearchResult snapshotSearchResult = server.getMarketDataSnapshotMaster().search(snapshotSearchRequest);
      UniqueId snapshotId = snapshotSearchResult.getSingleSnapshot().getUniqueId();

      // look up the view definition by name to get the ID
      ConfigSearchRequest<ViewDefinition> configSearchRequest = new ConfigSearchRequest<>(ViewDefinition.class);
      configSearchRequest.setName(viewDefName);
      ConfigSearchResult<ViewDefinition> configSearchResult = server.getConfigMaster().search(configSearchRequest);
      UniqueId viewDefId = configSearchResult.getSingleValue().getValue().getUniqueId();

      List<MarketDataSpecification> marketDataSpecs =
          Lists.<MarketDataSpecification>newArrayList(new UserMarketDataSpecification(snapshotId));
      ViewCycleExecutionOptions cycleOptions =
          ViewCycleExecutionOptions
              .builder()
              .setValuationTime(valuationTime)
              .setMarketDataSpecifications(marketDataSpecs)
              .setResolverVersionCorrection(VersionCorrection.LATEST)
              .create();
      EnumSet<ViewExecutionFlags> flags = ExecutionFlags.triggersEnabled().get();
      ArbitraryViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(ImmutableList.of(cycleOptions));
      ViewExecutionOptions executionOptions = ExecutionOptions.of(sequence, cycleOptions, flags);

      ViewProcessor viewProcessor = server.getViewProcessor();
      ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
      CountDownLatch latch = new CountDownLatch(1);
      viewClient.setResultListener(new Listener(server.getPositionSource(), latch));
      viewClient.setResultMode(ViewResultMode.FULL_ONLY);
      System.out.println("attaching to view process");
      viewClient.attachToViewProcess(viewDefId, executionOptions, true);
      System.out.println("waiting for completion");
      viewClient.waitForCompletion();
      System.out.println("view client completed");
      process.destroy();
      latch.await();
    }
    System.exit(0);
  }

  /**
   * Starts a thread which consumes a stream line by line and puts a value onto a queue when it encounters
   * a particular value. If an exception occurs false it put onto the queue.
   * @param stream The stream to consume
   * @param value The trigger value - the stream line must <em>startWith</em> this string
   * @param queue The queue
   * @param signalValue The value to put onto the queue when line is encountered in the stream
   */
  private static void consumeStream(final InputStream stream,
                                    final String value,
                                    final BlockingQueue<Boolean> queue,
                                    final Boolean signalValue,
                                    final PrintStream output) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          String nextLine;
          while ((nextLine = reader.readLine()) != null) {
            output.println(nextLine);
            if (nextLine.startsWith(value)) {
              queue.put(signalValue);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          try {
            queue.put(false);
          } catch (InterruptedException e1) {
            // not going to happen
          }
        } catch (InterruptedException e) {
          // not going to happen
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }
}

class Listener extends AbstractViewResultListener {

  private final AtomicReference<CompiledViewDefinition> _viewDef = new AtomicReference<>();
  private final PositionSource _positionSource;
  private final CountDownLatch _latch;

  Listener(PositionSource positionSource, CountDownLatch latch) {
    ArgumentChecker.notNull(positionSource, "");
    ArgumentChecker.notNull(latch, "latch");
    _latch = latch;
    _positionSource = positionSource;
  }

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    System.out.println("view def compiled");
    _viewDef.set(compiledViewDefinition);
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    System.out.println("view def compilation failed " + exception);
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    System.out.println("process terminated");
  }

  @Override
  public void processCompleted() {
    System.out.println("process completed");
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    try {
      System.out.println("cycle completed");
      CalculationResults results = CalculationResults.create(_viewDef.get(), fullResult, _positionSource);
      System.out.println(results);
    } finally {
      _latch.countDown();
    }
  }
}
