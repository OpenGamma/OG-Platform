/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 */
/* package */ class ViewRunner {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewRunner.class);

  private final ConfigMaster _configMaster;
  private final ViewProcessor _viewProcessor;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final MarketDataSnapshotMaster _snapshotMaster;

  /* package */ ViewRunner(ConfigMaster configMaster,
                           ViewProcessor viewProcessor,
                           PositionSource positionSource,
                           SecuritySource securitySource,
                           MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _configMaster = configMaster;
    _viewProcessor = viewProcessor;
    _positionSource = positionSource;
    _snapshotMaster = snapshotMaster;
  }

  // TODO convert to a tool
  /* TODO inputs
  working directories (and versions?) for each of the two servers
  views/snapshots/valuation times
    is it possible to infer it all? run each view using the snapshots which have it as their basis view?
    either have a fixed valuation time or use Instant.now() (but only once for all runs)
    where does the DB dump live? presumably with the test config. separate repo / project? part of integration tests repo?
    or checked into the main repo?

    after 'mvn install' the server is in
    $PROJECT_DIR/server/target/server-dir
   */
  public static void main(String[] args) throws Exception {
    Instant valuationTime = Instant.now();
    int serverHttpPort = 8080;
    String workingDir = System.getProperty("user.dir");
    String configFile = "classpath:fullstack/fullstack-examplessimulated-bin.properties";
    String projectName = "examples-simulated";
    String version = "2.2.0-SNAPSHOT";
    String serverJar = projectName + "-" + version + ".jar";
    String classpath = "config:lib/" + serverJar;
    String logbackConfig = "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml";
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, configFile, new Properties(), logbackConfig);
         RemoteServer server = RemoteServer.create("http://localhost:" + serverHttpPort))  {
      ViewRunner viewRunner = new ViewRunner(server.getConfigMaster(),
                                             server.getViewProcessor(),
                                             server.getPositionSource(),
                                             server.getSecuritySource(),
                                             server.getMarketDataSnapshotMaster());
      CalculationResults results1 = viewRunner.run(
          version,
          "AUD Swaps (3m / 6m basis) (1)",
          "AUD Swaps (3m / 6m basis) (1)/2013-09-27T12:17:45.587Z", valuationTime);
      CalculationResults results2 = viewRunner.run(
          version,
          "AUD Swaps (3m / 6m basis) (1)",
          "AUD Swaps (3m / 6m basis) (1)/2013-09-27T12:17:45.587Z", valuationTime);
      CalculationDifference difference = CalculationDifference.between(results1, results2, 0.001d);
      System.out.println(difference);
    }
  }

  public CalculationResults run(String version, String viewName, String snapshotName, Instant valuationTime) {
    ArgumentChecker.notNull(viewName, "viewName");
    ArgumentChecker.notNull(snapshotName, "snapshotName");
    UniqueId snapshotId = getSnapshotId(snapshotName);
    UniqueId viewDefId = getViewDefinitionId(viewName);
    s_logger.info("Running view {} using snapshot {} at valuation time {}", viewName, snapshotName, valuationTime);
    List<MarketDataSpecification> marketDataSpecs =
        Lists.<MarketDataSpecification>newArrayList(UserMarketDataSpecification.of(snapshotId));
    ViewCycleExecutionOptions cycleOptions =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(valuationTime)
            .setMarketDataSpecifications(marketDataSpecs) // TODO multiple snapshots without rebuilding the graph?
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.triggersEnabled().get();
    ArbitraryViewCycleExecutionSequence sequence =
        new ArbitraryViewCycleExecutionSequence(ImmutableList.of(cycleOptions));
    ViewExecutionOptions executionOptions = ExecutionOptions.of(sequence, cycleOptions, flags);

    ViewProcessor viewProcessor = _viewProcessor;
    ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    Listener listener = new Listener(_positionSource, _securitySource, snapshotName, version);
    viewClient.setResultListener(listener);
    viewClient.setResultMode(ViewResultMode.FULL_ONLY);
    System.out.println("attaching to view process for view definition '" + viewName + "' with snapshot '" + snapshotName + "'");
    viewClient.attachToViewProcess(viewDefId, executionOptions, true);
    System.out.println("waiting for completion");
    try {
      viewClient.waitForCompletion();
      System.out.println("view client completed");
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted waiting for view client to complete", e);
    }
    viewClient.shutdown();
    return listener.getResults();
  }

  private UniqueId getSnapshotId(String snapshotName) {
    //String snapshotTime = "2013-09-27T12:17:45.587Z";
    //String snapshotName = snapshotName + "/" + snapshotTime;
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setName(snapshotName);
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    return snapshotSearchResult.getSingleSnapshot().getUniqueId();
  }

  private UniqueId getViewDefinitionId(String viewDefName) {
    ConfigSearchRequest<ViewDefinition> configSearchRequest = new ConfigSearchRequest<>(ViewDefinition.class);
    configSearchRequest.setName(viewDefName);
    ConfigSearchResult<ViewDefinition> configSearchResult = _configMaster.search(configSearchRequest);
    return configSearchResult.getSingleValue().getValue().getUniqueId();
  }
}

class Listener extends AbstractViewResultListener {

  private final AtomicReference<CompiledViewDefinition> _viewDef = new AtomicReference<>();
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final CountDownLatch _latch = new CountDownLatch(1);
  private final String _version;
  private final String _snapshotName;

  private CalculationResults _results;

  Listener(PositionSource positionSource,
           SecuritySource securitySource,
           String snapshotName,
           String version) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notEmpty(snapshotName, "snapshotName");
    ArgumentChecker.notEmpty(version, "version");
    _version = version;
    _securitySource = securitySource;
    _snapshotName = snapshotName;
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
      System.out.println("cycle completed - building CalculationResults object");
      _results = CalculationResults.create(fullResult, _viewDef.get(), _snapshotName, fullResult.getViewCycleExecutionOptions().getValuationTime(),
                                           _version, _positionSource, _securitySource);
      System.out.println("built CalculationResults object");
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
