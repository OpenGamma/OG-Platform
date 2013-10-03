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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
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
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 */
public class ViewRunner {

  // TODO convert to a tool
  /* TODO inputs
  working directories (and versions?) for each of the two servers
  views/snapshots/valuation times
    is it possible to infer it all? run each view using the snapshots which have it as their basis view?
    either have a fixed valuation time or use Instant.now() (but only once for all runs)
    where does the DB dump live? presumably with the test config. separate repo / project? part of integration tests repo?
    or checked into the main repo?
   */
  public static void main(String[] args) throws Exception {
    Instant valuationTime = Instant.now();
    //CalculationResults results1 = ViewRunner.run(valuationTime);
    //CalculationResults results2 = ViewRunner.run(valuationTime);
    //CalculationDifference.Result result = CalculationDifference.compare(results1, results2, 0.001d);
    //System.out.println(result);
  }

  /**
   * This is a workaround for PLAT-4793
   */
  private SetMultimap<UniqueId, UniqueId> getSnapshotIdsByViewDefId(MarketDataSnapshotMaster snapshotMaster,
                                                                    ConfigMaster configMaster) {
    SetMultimap<UniqueId, UniqueId> multiMap = HashMultimap.create();
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    // TODO horribly inefficient but a workaround for PLAT-4793
    searchRequest.setIncludeData(true);
    for (MarketDataSnapshotDocument doc : MarketDataSnapshotSearchIterator.iterable(snapshotMaster, searchRequest)) {
      ManageableMarketDataSnapshot snapshot = doc.getSnapshot();
      ConfigSearchRequest<ViewDefinition> configSearchRequest = new ConfigSearchRequest<>(ViewDefinition.class);
      String basisViewName = snapshot.getBasisViewName();
      if (basisViewName == null) {
        continue;
      }
      configSearchRequest.setName(basisViewName);
      ConfigSearchResult<ViewDefinition> configSearchResult = configMaster.search(configSearchRequest);
      if (configSearchResult.getValues().size() == 1) {
        UniqueId viewDefId = configSearchResult.getFirstValue().getValue().getUniqueId();
        multiMap.put(viewDefId, snapshot.getUniqueId());
      }
    }
    return multiMap;
  }

  /*private static UniqueId getSnapshotId(String viewDefName, MarketDataSnapshotMaster snapshotMaster) {
    String snapshotTime = "2013-09-27T12:17:45.587Z";
    String snapshotName = viewDefName + "/" + snapshotTime;
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setName(snapshotName);
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = snapshotMaster.search(snapshotSearchRequest);
    return snapshotSearchResult.getSingleSnapshot().getUniqueId();
  }*/

  /*private static UniqueId getViewDefinitionId(String viewDefName, ConfigMaster configMaster) {
    ConfigSearchRequest<ViewDefinition> configSearchRequest = new ConfigSearchRequest<>(ViewDefinition.class);
    configSearchRequest.setName(viewDefName);
    ConfigSearchResult<ViewDefinition> configSearchResult = configMaster.search(configSearchRequest);
    return configSearchResult.getSingleValue().getValue().getUniqueId();
  }*/

  public static CalculationResults run(Instant valuationTime, UniqueId viewDefId, UniqueId snapshotId) {
    int serverHttpPort = 8080;
    // TODO get server details (port. any others? URL?) from the server process?
    try (ServerProcess ignored = ServerProcess.start();
         RemoteServer server = RemoteServer.create("http://localhost:" + serverHttpPort)) {
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
      Listener listener = new Listener(server.getPositionSource());
      viewClient.setResultListener(listener);
      viewClient.setResultMode(ViewResultMode.FULL_ONLY);
      System.out.println("attaching to view process");
      viewClient.attachToViewProcess(viewDefId, executionOptions, true);
      System.out.println("waiting for completion");
      viewClient.waitForCompletion();
      System.out.println("view client completed");
      viewClient.shutdown();
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
  private final CountDownLatch _latch = new CountDownLatch(1);

  private CalculationResults _results;

  Listener(PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "");
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
    try {
      _latch.await();
    } catch (InterruptedException e) {
      // not going to happen
      throw new OpenGammaRuntimeException("unexpected exception", e);
    }
    return _results;
  }
}
