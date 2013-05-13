/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
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
import com.opengamma.util.time.DateUtils;

/**
 * Utilities to support database testing.
 */
public final class DbTest {

  /** Known dialects. */
  private static final Map<String, DbDialect> s_dbDialects = new ConcurrentHashMap<>();

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
   * @param return the known database dialects keyed by type, not null
   */
  public static Map<String, DbDialect> getSupportedDbDialects() {
    return new HashMap<>(s_dbDialects);
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
  public static Object[][] data_localDatabase() {
    return getParametersForDatabase("hsqldb");
  }

  @DataProvider(name = "databases")
  public static Object[][] data_databases() {
    try {
      return getParameters();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

  @DataProvider(name = "databasesVersionsForSeparateMasters")
  public static Object[][] data_databasesVersionsForSeparateMasters() {
    return getParametersForSeparateMasters(3);
  }

  //-------------------------------------------------------------------------
  private static Object[][] getParametersForSeparateMasters(int prevVersionCount) {
    Collection<String> databaseTypes = getAvailableDatabaseTypes(System.getProperty("test.database.type"));
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String databaseType : databaseTypes) {
      DbScripts scripts = DbScripts.of(DbScripts.getSqlScriptDir(), databaseType);
      Map<String, SortedMap<Integer, DbScriptPair>> scriptPairs = scripts.getScriptPairs();
      for (String schema : scriptPairs.keySet()) {
        Set<Integer> versions = scriptPairs.get(schema).keySet();
        int max = Collections.max(versions);
        int min = Collections.min(versions);
        for (int v = max; v >= Math.max(max - prevVersionCount, min); v--) {
          parameters.add(new Object[]{databaseType, schema, "" + max /*target_version*/, "" + v /*migrate_from_version*/});
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
      DbDialect dbDialect = s_dbDialects.get(dbType);
      try {
        Objects.requireNonNull(dbDialect.getJDBCDriverClass());
      } catch (RuntimeException | Error ex) {
        System.err.println("Database driver not available: " + dbDialect);
        it.remove();
      }
    }
    return databaseTypes;
  }

  /**
   * Creates a {@code DbTool} for a specific database.
   * The connector may be passed in to share if it exists already.
   * 
   * @param databaseType  the database type, not null
   * @param connector  the connector, null if not to be shared
   * @return the tool, not null
   */
  public static DbTool createDbTool(String databaseType, DbConnector connector) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    String dbHost = getDbHost(databaseType);
    String user = getDbUsername(databaseType);
    String password = getDbPassword(databaseType);
    DataSource dataSource = (connector != null ? connector.getDataSource() : null);
    DbTool dbTool = new DbTool(dbHost, user, password, dataSource);
    dbTool.initialize();
    dbTool.setJdbcUrl(dbTool.getTestDatabaseUrl());
    dbTool.addDbScriptDirectories(DbScripts.getSqlScriptDir());
    return dbTool;
  }

  //-------------------------------------------------------------------------
  public static String getDbHost(String databaseType) {
    String dbHostProperty = databaseType + ".jdbc.url";
    String dbHost = TestProperties.getTestProperties().getProperty(dbHostProperty);
    if (dbHost == null) {
      throw new OpenGammaRuntimeException("Property " + dbHostProperty
          + " not found");
    }
    return dbHost;
  }

  public static String getDbUsername(String databaseType) {
    String userProperty = databaseType + ".jdbc.username";
    String user = TestProperties.getTestProperties().getProperty(userProperty);
    if (user == null) {
      throw new OpenGammaRuntimeException("Property " + userProperty
          + " not found");
    }
    return user;
  }

  public static String getDbPassword(String databaseType) {
    String passwordProperty = databaseType + ".jdbc.password";
    String password = TestProperties.getTestProperties().getProperty(passwordProperty);
    if (password == null) {
      throw new OpenGammaRuntimeException("Property " + passwordProperty
          + " not found");
    }
    return password;
  }

}
