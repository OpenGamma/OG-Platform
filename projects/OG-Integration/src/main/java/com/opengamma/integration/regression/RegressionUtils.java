/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 *
 */
/* package */final class RegressionUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(RegressionUtils.class);

  /** Name of the ID mappings in the config database. */
  public static final String ID_MAPPINGS = "Regression test ID mappings";
  /** Name of the ID mappings Fudge XML file. */
  public static final String ID_MAPPINGS_IDENTIFIER = "idMappings";
  /** Name of the ref data accesses Fudge XML file */
  public static final String REF_DATA_ACCESSES_IDENTIFIER = "refDataAccesses";
  
  /** Name of the ID mappings Fudge XML file. */
  public static final String ID_MAPPINGS_FILE = ID_MAPPINGS_IDENTIFIER + FudgeXMLFormat.FILE_EXTENSION;

  /** Type identifier for SecurityMaster data. */
  public static final String SECURITY_MASTER_DATA = "securities";
  /** Type identifier for PositionMaster data. */
  public static final String POSITION_MASTER_DATA = "positions";
  /** Type identifier for PortfolioMaster data. */
  public static final String PORTFOLIO_MASTER_DATA = "portfolios";
  /** Type identifier for ConfigMaster data. */
  public static final String CONFIG_MASTER_DATA = "configs";
  /** Type identifier for HistoricalTimeSeriesMaster data. */
  public static final String HISTORICAL_TIME_SERIES_MASTER_DATA = "timeseries";
  /** Type identifier for HolidayMaster data. */
  public static final String HOLIDAY_MASTER_DATA = "holidays";
  /** Type identifier for ExchangeMaster data. */
  public static final String EXCHANGE_MASTER_DATA = "exchanges";
  /** Type identifier for MarketDataSnapshotMaster data. */
  public static final String MARKET_DATA_SNAPSHOT_MASTER_DATA = "snapshots";
  /** Type identifier for LegalEntityMaster data. */
  public static final String LEGAL_ENTITY_MASTER_DATA = "legalentities";
  /** Type identifier for ConventionMaster data. */
  public static final String CONVENTION_MASTER_DATA = "conventions";

  private RegressionUtils() {
  }

  /**
   * Creates an empty database by running {@link EmptyDatabaseCreator} in an external process. {@code EmptyDatabaseCreator} relies on {@code DbTool} which scans the classpath to locate the schema
   * files. This means it has to run with the classpath of the server version being tested so it can find the correct schema files.
   */
  /* package */static void createEmptyDatabase(String configFile, String workingDirName, String classpath, String logbackConfig) {
    // TODO load the config and check the DB URL is overridden. ensure we NEVER use the URL from the real server config
    // TODO configurable java command
    Process process = null;
    try {
      String className = EmptyDatabaseCreator.class.getName();
      File workingDir = new File(workingDirName);
      s_logger.info("Launching EmptyDatabaseCreator process. workingDir={}, logbackConfig={}, className={}, " + "configFile={}, classpath={}", workingDir, logbackConfig, className,
          configFile, classpath);
      process = new ProcessBuilder("java", logbackConfig, "-cp", classpath, className, configFile).directory(workingDir).redirectOutput(ProcessBuilder.Redirect.INHERIT)
          .redirectError(ProcessBuilder.Redirect.INHERIT).start();
      process.waitFor();
      int exitCode = process.exitValue();
      if (exitCode != 0) {
        throw new OpenGammaRuntimeException("Failed to create database, exit code: " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new OpenGammaRuntimeException("Failed to create database", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /* package */static void restoreDatabase(String workingDir, String classpath, Properties dbProps, String serverConfigFile, String logbackConfig, String databaseDumpDir) {
    // TODO don't hard-code the port
    int port = 8080;
    String serverUrl = "http://localhost:" + port;
    // run the server, populate the database and stop the server.
    // it needs to be restarted before the tests to pick up function repo changes from the database
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, serverConfigFile, dbProps, logbackConfig); RemoteServer server = RemoteServer.create(serverUrl)) {
      DatabaseRestore databaseRestore = new DatabaseRestore(databaseDumpDir, server.getSecurityMaster(), server.getPositionMaster(), server.getPortfolioMaster(), server.getConfigMaster(),
          server.getHistoricalTimeSeriesMaster(), server.getHolidayMaster(), server.getExchangeMaster(), server.getMarketDataSnapshotMaster(), server.getLegalEntityMaster(),
          server.getConventionMaster());
      databaseRestore.restoreDatabase();
    }
  }

  /* package */static Properties loadProperties(String propsFile) {
    try {
      Properties properties = new Properties();
      properties.load(new BufferedInputStream(new FileInputStream(propsFile)));
      return properties;
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to load properties", e);
    }
  }

  static ConfigItem<IdMappings> loadIdMappings(ConfigMaster configMaster) {
    ConfigSearchRequest<IdMappings> request = new ConfigSearchRequest<>(IdMappings.class);
    request.setName(ID_MAPPINGS);
    ConfigSearchResult<IdMappings> result = configMaster.search(request);
    if (result.getValues().size() == 1) {
      return result.getFirstValue();
    } else {
      return null;
    }
  }
}
