/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class ViewRegressionTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewRegressionTest.class);
  // TODO arg for this? different deltas for different values and/or object types?

  private final String _dbDumpDir;
  private final Instant _valuationTime;
  private final String _baseWorkingDir;
  private final String _baseVersion;
  private final String _baseDbConfigFile;
  private final String _testWorkingDir;
  private final String _serverConfigFile;
  private final String _testVersion;
  private final String _testDbConfigFile;
  private final String _logbackConfig;
  private final String _baseClasspath;
  private final String _testClasspath;

  public ViewRegressionTest(String projectName,
                            String serverConfigFile,
                            String dbDumpDir,
                            String logbackConfigFile,
                            Instant valuationTime,
                            String baseWorkingDir,
                            String baseVersion,
                            String baseDbConfigFile,
                            String testWorkingDir,
                            String testVersion,
                            String testDbConfigFile) {
    _dbDumpDir = dbDumpDir;
    _baseWorkingDir = baseWorkingDir;
    _baseVersion = baseVersion;
    _baseDbConfigFile = baseDbConfigFile;
    _testWorkingDir = testWorkingDir;
    _serverConfigFile = serverConfigFile;
    _testVersion = testVersion;
    _testDbConfigFile = testDbConfigFile;
    _logbackConfig = "-Dlogback.configurationFile=" + logbackConfigFile;
    _baseClasspath = "config:lib/" + projectName + "-" + baseVersion + ".jar";
    _testClasspath = "config:lib/" + projectName + "-" + testVersion + ".jar";
    _valuationTime = valuationTime;
  }

  public RegressionTestResults run() {
    // TODO store the results in memory for now, serialize to disk/cache when it's an actual problem
    // TODO fail if there are any view defs or snapshots with duplicate names
    Map<Pair<String, String>, CalculationResults> testResults =
        runTest(_testWorkingDir, _testClasspath, _testVersion, _testDbConfigFile);
    Map<Pair<String, String>, CalculationResults> baseResults =
        runTest(_baseWorkingDir, _baseClasspath, _baseVersion, _baseDbConfigFile);
    List<CalculationDifference> results = Lists.newArrayList();
    for (Map.Entry<Pair<String, String>, CalculationResults> entry : testResults.entrySet()) {
      CalculationResults testViewResult = entry.getValue();
      CalculationResults baseViewResult = baseResults.get(entry.getKey());
      if (baseViewResult == null) {
        s_logger.warn("No base result for {}", entry.getKey());
        continue;
      }
      results.add(CalculationDifference.between(baseViewResult, testViewResult, ViewRegressionTestTool.DELTA));
    }
    return new RegressionTestResults(_baseVersion, _testVersion, results);
  }

  private Map<Pair<String, String>, CalculationResults> runTest(String workingDir,
                                                                String classpath,
                                                                String version,
                                                                String dbPropsFile) {
    // don't use the config file to be sure we don't accidentally clobber a real database
    Properties dbProps = RegressionUtils.loadProperties(dbPropsFile);
    if (_dbDumpDir != null) {
      RegressionUtils.createEmptyDatabase(dbPropsFile, workingDir, classpath, _logbackConfig);
      RegressionUtils.restoreDatabase(workingDir, classpath, dbProps, _serverConfigFile, _logbackConfig, _dbDumpDir);
    }
    return runViews(workingDir, classpath, version, _valuationTime, dbProps);
  }

  private Map<Pair<String, String>, CalculationResults> runViews(String workingDir,
                                                                 String classpath,
                                                                 String version,
                                                                 Instant valuationTime,
                                                                 Properties dbProps) {
    // TODO don't hard-code the port
    int port = 8080;
    String serverUrl = "http://localhost:" + port;

    // start the server again to run the tests
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, _serverConfigFile, dbProps, _logbackConfig);
         RemoteServer server = RemoteServer.create(serverUrl)) {
      Map<Pair<String, String>, CalculationResults> allResults = Maps.newHashMap();
      Collection<Pair<String, String>> viewAndSnapshotNames = getViewAndSnapshotNames(server.getConfigMaster(),
                                                                                      server.getMarketDataSnapshotMaster());
      ViewRunner viewRunner = new ViewRunner(server.getConfigMaster(),
                                             server.getViewProcessor(),
                                             server.getPositionSource(),
                                             server.getSecuritySource(),
                                             server.getMarketDataSnapshotMaster());
      for (Pair<String, String> names : viewAndSnapshotNames) {
        String viewName = names.getFirst();
        String snapshotName = names.getSecond();
        CalculationResults results = viewRunner.run(version, viewName, snapshotName, valuationTime);
        allResults.put(names, results);
      }
      return allResults;
    }
  }

  private static Collection<Pair<String, String>> getViewAndSnapshotNames(ConfigMaster configMaster,
                                                                          MarketDataSnapshotMaster snapshotMaster) {
    List<Pair<String, String>> viewAndSnapshotNames = Lists.newArrayList();
    MarketDataSnapshotSearchRequest snapshotRequest = new MarketDataSnapshotSearchRequest();
    // TODO this isn't great but is necessary because of PLAT-4793
    snapshotRequest.setIncludeData(true);
    MarketDataSnapshotSearchResult snapshotResult = snapshotMaster.search(snapshotRequest);
    for (ManageableMarketDataSnapshot snapshot : snapshotResult.getSnapshots()) {
      String basisViewName = snapshot.getBasisViewName();
      if (basisViewName != null) {
        ConfigSearchRequest<ViewDefinition> configRequest = new ConfigSearchRequest<>(ViewDefinition.class);
        configRequest.setName(basisViewName);
        ConfigSearchResult<ViewDefinition> configResult = configMaster.search(configRequest);
        if (configResult.getValues().size() > 1) {
          s_logger.warn("Multiple view definitions found with the same name '{}'", basisViewName);
          continue;
        }
        String viewDefName = configResult.getSingleValue().getName();
        viewAndSnapshotNames.add(Pairs.of(viewDefName, snapshot.getName()));
      }
    }
    return viewAndSnapshotNames;
  }
}
