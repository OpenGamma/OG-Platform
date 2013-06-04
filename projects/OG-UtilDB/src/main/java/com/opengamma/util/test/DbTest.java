/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.testng.annotations.DataProvider;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.HSQLDbDialect;
import com.opengamma.util.db.PostgresDbDialect;
import com.opengamma.util.db.SqlServer2008DbDialect;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Utilities to support database testing.
 */
public final class DbTest {

  /** Known dialects. */
  private static final Map<String, DbDialect> s_dbDialects = new ConcurrentHashMap<>();
  /** Available dialects. */
  private static final Map<String, Boolean> s_availableDialects = new ConcurrentHashMap<String, Boolean>();

  static {
    // initialize the clock
    DateUtils.initTimeZone();
    
    // setup the known databases
    addDbDialect("hsqldb", new HSQLDbDialect());
    addDbDialect("postgres", new PostgresDbDialect());
    addDbDialect("sqlserver2008", new SqlServer2008DbDialect());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  private DbTest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the supported databases for testing.
   * 
   * @return the supported database types, not null
   */
  public static Collection<String> getSupportedDatabaseTypes() {
    return new ArrayList<>(s_dbDialects.keySet());
  }

  /**
   * Gets the selected database types.
   * 
   * @return a singleton collection containing the String passed in, except if the type is ALL
   *  (case insensitive), in which case all supported database types are returned, not null
   */
  static Collection<String> initDatabaseTypes(String commandLineDbType) {
    ArrayList<String> dbTypes = new ArrayList<String>();
    if (commandLineDbType.trim().equalsIgnoreCase("all")) {
      dbTypes.addAll(getSupportedDatabaseTypes());
    } else {
      dbTypes.add(commandLineDbType);
    }
    return dbTypes;
  }

  /**
   * Gets the known dialects.
   *
   * @return the known database dialects keyed by type, not null
   */
  public static Map<String, DbDialect> getSupportedDbDialects() {
    return new HashMap<>(s_dbDialects);
  }

  /**
   * Gets the known dialect, checking it is known.
   * 
   * @param databaseType  the database type, not null
   * @return the dialect, not null
   */
  public static DbDialect getSupportedDbDialect(String databaseType) {
    DbDialect dbDialect = getSupportedDbDialects().get(databaseType);
    if (dbDialect == null) {
      throw new OpenGammaRuntimeException("Config error - no DbDialect setup for " + databaseType);
    }
    return dbDialect;
  }

  /**
   * Adds a dialect to the map of known.
   *
   * @param dbType  the database type, not null
   * @param dialect  the dialect, not null
   */
  public static void addDbDialect(String dbType, DbDialect dialect) {
    s_dbDialects.put(dbType, dialect);
  }

  //-------------------------------------------------------------------------
  @DataProvider(name = "localDatabase")
  public static Object[][] data_localDatabase() {  // CSIGNORE
    Object[][] data = getParametersForDatabase("hsqldb");
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  @DataProvider(name = "databases")
  public static Object[][] data_databases() {  // CSIGNORE
    Object[][] data = getParameters();
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  @DataProvider(name = "databasesVersionsForSeparateMasters")
  public static Object[][] data_databasesVersionsForSeparateMasters() {  // CSIGNORE
    Object[][] data = getParametersForSeparateMasters(3);
    if (data.length == 0) {
      throw new IllegalStateException("No databases available");
    }
    return data;
  }

  //-------------------------------------------------------------------------
  private static Object[][] getParametersForSeparateMasters(int prevVersionCount) {
    Collection<String> databaseTypes = getAvailableDatabaseTypes(System.getProperty("test.database.type"));
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (DbSchemaGroupMetadata schemaGroupMetadata : DbScriptUtils.getAllSchemaGroupMetadata()) {
      for (String databaseType : databaseTypes) {
        int max = schemaGroupMetadata.getCurrentVersion();
        int min = max;
        while (schemaGroupMetadata.getCreateScript(databaseType, min - 1) != null) {
          min--;
        }
        for (int v = max; v >= Math.max(max - prevVersionCount, min); v--) {
          parameters.add(new Object[]{databaseType, schemaGroupMetadata.getSchemaGroupName(), max /*target_version*/, v /*migrate_from_version*/});
        }
      }      
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  private static Object[][] getParameters() {
    Collection<String> databaseTypes = getAvailableDatabaseTypes(System.getProperty("test.database.type"));
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String databaseType : databaseTypes) {
      parameters.add(new Object[]{databaseType, "latest"});
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  private static Object[][] getParametersForDatabase(final String databaseType) {
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String db : getAvailableDatabaseTypes(databaseType)) {
      parameters.add(new Object[]{db, "latest"});
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  /**
   * Not all database drivers are available in some test environments.
   */
  private static Collection<String> getAvailableDatabaseTypes(String databaseType) {
    Collection<String> databaseTypes;
    if (databaseType == null) {
      databaseTypes = Sets.newHashSet(s_dbDialects.keySet());
    } else {
      if (s_dbDialects.containsKey(databaseType) == false) {
        throw new IllegalArgumentException("Unknown database: " + databaseType);
      }
      databaseTypes = Sets.newHashSet(databaseType);
    }
    for (Iterator<String> it = databaseTypes.iterator(); it.hasNext(); ) {
      String dbType = it.next();
      Boolean available = s_availableDialects.get(dbType);
      if (available == null) {
        DbDialect dbDialect = s_dbDialects.get(dbType);
        try {
          Objects.requireNonNull(dbDialect.getJDBCDriverClass());
          available = true;
        } catch (RuntimeException | Error ex) {
          available = false;
          System.err.println("Database driver not available: " + dbType);
        }
        s_availableDialects.put(dbType, available);
      }
      if (available == false) {
        it.remove();
      }
    }
    return databaseTypes;
  }

  /**
   * Creates a {@code DbTool} for a specific database.
   * The connector may be passed in to share if it exists already.
   * 
   * @param databaseConfigPrefix  the prefix for a database in the config file, not null
   * @param connector  the connector, null if not to be shared
   * @return the tool, not null
   */
  public static DbTool createDbTool(String databaseConfigPrefix, DbConnector connector) {
    ArgumentChecker.notNull(databaseConfigPrefix, "databaseConfigPrefix");
    String dbHost = getDbHost(databaseConfigPrefix);
    String user = getDbUsername(databaseConfigPrefix);
    String password = getDbPassword(databaseConfigPrefix);
    DataSource dataSource = (connector != null ? connector.getDataSource() : null);
    DbTool dbTool = new DbTool(dbHost, user, password, dataSource);
    dbTool.initialize();
    dbTool.setJdbcUrl(dbTool.getTestDatabaseUrl());
    return dbTool;
  }

  //-------------------------------------------------------------------------
  public static DbDialect getDbType(String databaseConfigPrefix) {
    String dbTypeProperty = databaseConfigPrefix + ".jdbc.type";
    String dbType = TestProperties.getTestProperties().getProperty(dbTypeProperty);
    if (dbType == null) {
      throw new OpenGammaRuntimeException("Property " + dbTypeProperty + " not found");
    }
    return getSupportedDbDialect(dbType);
  }

  public static String getDbHost(String databaseConfigPrefix) {
    String dbHostProperty = databaseConfigPrefix + ".jdbc.url";
    String dbHost = TestProperties.getTestProperties().getProperty(dbHostProperty);
    if (dbHost == null) {
      throw new OpenGammaRuntimeException("Property " + dbHostProperty + " not found");
    }
    return dbHost;
  }

  public static String getDbUsername(String databaseConfigPrefix) {
    String userProperty = databaseConfigPrefix + ".jdbc.username";
    String user = TestProperties.getTestProperties().getProperty(userProperty);
    if (user == null) {
      throw new OpenGammaRuntimeException("Property " + userProperty + " not found");
    }
    return user;
  }

  public static String getDbPassword(String databaseConfigPrefix) {
    String passwordProperty = databaseConfigPrefix + ".jdbc.password";
    String password = TestProperties.getTestProperties().getProperty(passwordProperty);
    if (password == null) {
      throw new OpenGammaRuntimeException("Property " + passwordProperty + " not found");
    }
    return password;
  }

}
