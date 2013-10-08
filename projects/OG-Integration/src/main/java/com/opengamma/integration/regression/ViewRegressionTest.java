/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class ViewRegressionTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewRegressionTest.class);
  // TODO arg for this? different deltas for different values and/or object types?
  private static final double DELTA = 0.001;

  private final String _databaseDumpDir;
  private final Instant _valuationTime;
  private final String _baseWorkingDir;
  private final String _baseDbConfigFile;
  private final String _newWorkingDir;
  private final String _serverConfigFile;
  private final String _newDbConfigFile;
  private final String _logbackConfig;
  private final String _baseClasspath;
  private final String _newClasspath;

  public ViewRegressionTest(String projectName,
                            String serverConfigFile,
                            String databaseDumpDir,
                            String logbackConfigFile,
                            Instant valuationTime,
                            String baseWorkingDir,
                            String baseVersion,
                            String baseDbConfigFile,
                            String newWorkingDir,
                            String newVersion,
                            String newDbConfigFile) {
    _databaseDumpDir = databaseDumpDir;
    _baseWorkingDir = baseWorkingDir;
    _baseDbConfigFile = baseDbConfigFile;
    _newWorkingDir = newWorkingDir;
    _serverConfigFile = serverConfigFile;
    _newDbConfigFile = newDbConfigFile;
    _logbackConfig = "-Dlogback.configurationFile=" + logbackConfigFile;
    _baseClasspath = "config:lib/" + projectName + "-" + baseVersion + ".jar";
    _newClasspath = "config:lib/" + projectName + "-" + newVersion + ".jar";
    _valuationTime = valuationTime;
  }

  public Collection<CalculationDifference.Result> run() {
    // TODO store the results in memory for now, serialize to disk/cache when it's an actual problem
    // TODO fail if there are any view defs or snapshots with duplicate names
    Map<Pair<String, String>, CalculationResults> newResults = runTest(_newWorkingDir, _newClasspath, _newDbConfigFile);
    Map<Pair<String, String>, CalculationResults> baseResults = runTest(_baseWorkingDir, _baseClasspath, _baseDbConfigFile);
    List<CalculationDifference.Result> results = Lists.newArrayList();
    for (Map.Entry<Pair<String, String>, CalculationResults> entry : newResults.entrySet()) {
      CalculationResults newViewResult = entry.getValue();
      CalculationResults baseViewResult = baseResults.get(entry.getKey());
      if (baseViewResult == null) {
        s_logger.warn("No base result for {}", entry.getKey());
        continue;
      }
      results.add(CalculationDifference.compare(baseViewResult, newViewResult, DELTA));
    }
    return results;
  }

  private Map<Pair<String, String>, CalculationResults> runTest(String workingDir, String classpath, String dbPropsFile) {
    // don't use the config file to be sure we don't accidentally clobber a real database
    //createDatabase(dbPropsFile, workingDir, classpath, _logbackConfig);
    Properties dbProps = loadProperties(dbPropsFile);
    //restoreDatabase(workingDir, classpath, dbProps);
    return runViews(workingDir, classpath, _valuationTime, dbProps);
  }

  private static Properties loadProperties(String propsFile) {
    try {
      Properties properties = new Properties();
      properties.load(new BufferedInputStream(new FileInputStream(propsFile)));
      return properties;
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to load properties", e);
    }
  }

  private void restoreDatabase(String workingDir, String classpath, Properties dbProps) {
    // TODO don't hard-code the port
    int port = 8080;
    String serverUrl = "http://localhost:" + port;
    // run the server, populate the database and stop the server.
    // it needs to be restarted before the tests to pick up function repo changes from the database
    // TODO can this be done by creating DB masters directly rather than running a server and connecting remotely?
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, _serverConfigFile, dbProps, _logbackConfig);
         RemoteServer server = RemoteServer.create(serverUrl)) {
      DatabaseRestore.restoreDatabase(_databaseDumpDir, server);
    }
  }

  private Map<Pair<String, String>, CalculationResults> runViews(String workingDir,
                                                                 String classpath,
                                                                 Instant valuationTime,
                                                                 Properties dbProps) {
    // TODO don't hard-code the port
    int port = 8080;
    String serverUrl = "http://localhost:" + port;

    // start the server again to run the tests
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, _serverConfigFile, dbProps, _logbackConfig);
         // TODO don't hard-code the port
         RemoteServer server = RemoteServer.create(serverUrl)) {
      Map<Pair<String, String>, CalculationResults> allResults = Maps.newHashMap();
      Collection<Pair<String, String>> viewAndSnapshotNames = getViewAndSnapshotNames(server.getConfigMaster(),
                                                                                      server.getMarketDataSnapshotMaster());
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
        viewAndSnapshotNames.add(Pair.of(viewDefName, snapshot.getName()));
      }
    }
    return viewAndSnapshotNames;
  }

  /**
   * Creates an empty database by running {@link EmptyDatabaseCreator} in an external process.
   * {@code EmptyDatabaseCreator} relies on {@code DbTool} which scans the classpath to locate the schema files.
   * This means it has to run with the classpath of the server version being tested so it can find the correct
   * schema files.
   */
  /* package */ static int createDatabase(String configFile,
                                          String workingDirName,
                                          String classpath,
                                          String logbackConfig) {
    // TODO load the config and check the DB URL is overridden. ensure we NEVER use the URL from the real server config
    // TODO configurable java command
    Process process = null;
    try {
      String className = EmptyDatabaseCreator.class.getName();
      File workingDir = new File(workingDirName);
      process = new ProcessBuilder("java", logbackConfig, "-cp", classpath, className, configFile)
          .directory(workingDir)
          .redirectOutput(ProcessBuilder.Redirect.INHERIT)
          .redirectError(ProcessBuilder.Redirect.INHERIT)
          .start();
      process.waitFor();
      return process.exitValue();
    } catch (IOException | InterruptedException e) {
      throw new OpenGammaRuntimeException("Failed to create database", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }
}
