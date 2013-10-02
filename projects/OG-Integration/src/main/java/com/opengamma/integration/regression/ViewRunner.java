/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
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
/* package */ class ViewRunner {



  public static void main(String[] args) throws Exception {
    Instant valuationTime = Instant.now();
    CalculationResults results1 = ViewRunner.run(valuationTime);
    CalculationResults results2 = ViewRunner.run(valuationTime);
    CalculationDifference.Result result = CalculationDifference.compare(results1, results2, 0.01d);
    System.out.println(result);
  }

  public static CalculationResults run(Instant valuationTime) {
    int serverHttpPort = 8080;
    // TODO get server details (port. any others? URL?) from the server process
    try (ServerProcess serverProcess = ServerProcess.start();
         RemoteServer server = RemoteServer.create("http://localhost:" + serverHttpPort)) {
      String viewDefName = "AUD Swaps (3m / 6m basis) (1)";
      String snapshotTime = "2013-09-27T12:17:45.587Z";
      String snapshotName = viewDefName + "/" + snapshotTime;
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
      ArbitraryViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(ImmutableList.of(
          cycleOptions));
      ViewExecutionOptions executionOptions = ExecutionOptions.of(sequence, cycleOptions, flags);

      ViewProcessor viewProcessor = server.getViewProcessor();
      ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
      CountDownLatch latch = new CountDownLatch(1);
      Listener listener = new Listener(server.getPositionSource(), latch);
      viewClient.setResultListener(listener);
      viewClient.setResultMode(ViewResultMode.FULL_ONLY);
      System.out.println("attaching to view process");
      viewClient.attachToViewProcess(viewDefId, executionOptions, true);
      System.out.println("waiting for completion");
      viewClient.waitForCompletion();
      System.out.println("view client completed");
      latch.await();
      return listener.getResults();
    } catch (InterruptedException e) {
      // not going to happen but need this to satisfy the compiler
      throw new OpenGammaRuntimeException("unexpected exception", e);
    }
  }
}

class Listener extends AbstractViewResultListener {

  private final AtomicReference<CompiledViewDefinition> _viewDef = new AtomicReference<>();
  private final PositionSource _positionSource;
  private final CountDownLatch _latch;

  private CalculationResults _results;

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
      _results = CalculationResults.create(_viewDef.get(), fullResult, _positionSource);
    } finally {
      _latch.countDown();
    }
  }

  public CalculationResults getResults() {
    return _results;
  }
}
