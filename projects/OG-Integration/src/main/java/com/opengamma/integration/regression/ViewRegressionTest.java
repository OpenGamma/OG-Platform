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

import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class ViewRegressionTest {

  private final String _databaseDumpDir;
  private final String _baseWorkingDir;
  private final String _newWorkingDir;
  private final String _configFile;
  private final String _logbackConfig;
  private final String _databaseUrl; // TODO multiple URLs - standard, user, batch, hts
  private final String _classpath;

  public ViewRegressionTest(String databaseDumpDir,
                            String baseWorkingDir,
                            String newWorkingDir,
                            String configFile,
                            String serverJar,
                            String logbackConfig,
                            String databaseUrl) {
    _databaseDumpDir = databaseDumpDir;
    _baseWorkingDir = baseWorkingDir;
    _newWorkingDir = newWorkingDir;
    _configFile = configFile;
    _logbackConfig = logbackConfig;
    _databaseUrl = databaseUrl;
    _classpath = "config:lib/" + serverJar;
  }

  public Collection<CalculationDifference.Result> run() {
    // TODO store the results in memory for now, serialize to disk/cache when it's an actual problem
    // TODO fail if there are any view defs or snapshots with duplicate names
    Instant valuationTime = Instant.now();
    // TODO these need the database URLs, user names and passwords. separate DB props file for regression tests?
    // or command line args to the tool?
    Properties dbProps = new Properties();
    Map<Pair<String, String>, CalculationResults> newResults = runViews(_newWorkingDir, valuationTime, dbProps);
    Map<Pair<String, String>, CalculationResults> baseResults = runViews(_baseWorkingDir, valuationTime, dbProps);
    // TODO compare results
    throw new UnsupportedOperationException();
  }

  private Map<Pair<String, String>, CalculationResults> runViews(String workingDir,
                                                                 Instant valuationTime,
                                                                 Properties dbProps) {
    // don't use the config file to be sure we don't accidentally clobber a real database
    EmptyDatabaseCreator.createDatabases(dbProps);
    // TODO don't hard-code the port
    int port = 8080;
    String serverUrl = "http://localhost:" + port;
    // run the server, populate the database and stop the server.
    // it needs to be restarted before the tests to pick up function repo changes from the database
    try (ServerProcess ignored = ServerProcess.start(workingDir, _classpath, _configFile, dbProps, _logbackConfig);
         RemoteServer server = RemoteServer.create(serverUrl)) {
      DatabaseRestore.restoreDatabase(_databaseDumpDir, server);
    }
    // start the server again to run the tests
    try (ServerProcess ignored = ServerProcess.start(workingDir, _classpath, _configFile, dbProps, _logbackConfig);
         // TODO don't hard-code the port
         RemoteServer server = RemoteServer.create(serverUrl)) {
      Map<Pair<String, String>, CalculationResults> allResults = Maps.newHashMap();
      Collection<Pair<String, String>> viewAndSnapshotNames = getViewAndSnapshotNames(server);
      ViewRunner viewRunner = new ViewRunner(server.getConfigMaster(),
                                             server.getViewProcessor(),
                                             server.getPositionSource(),
                                             server.getMarketDataSnapshotMaster());
      for (Pair<String, String> names : viewAndSnapshotNames) {
        String viewName = names.getFirst();
        String snapshotName = names.getSecond();
        CalculationResults results = viewRunner.run(viewName, snapshotName, valuationTime);
        allResults.put(names, results);
      }
      return allResults;
    }
  }

  private static Collection<Pair<String, String>> getViewAndSnapshotNames(RemoteServer server) {
    Map<String, String> basisViewToSnapshot = Maps.newHashMap();
    /*
    load snapshots, create map(basisViewName->snapshotName) (includeData to get the basisViewName)
    for each combination of view and applicable snapshot, create a pair(viewName, snapshotName)
    */
    List<Pair<String, String>> viewAndSnapshotNames = Lists.newArrayList();
    return viewAndSnapshotNames;
  }
}
