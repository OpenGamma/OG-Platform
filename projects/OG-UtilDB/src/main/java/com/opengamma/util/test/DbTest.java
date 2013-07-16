/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.testng.annotations.DataProvider;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.db.tool.DbDialectUtils;
import com.opengamma.util.db.tool.DbTool;

/**
 * Utilities to support database testing.
 */
public final class DbTest {

  /**
   * Creates an instance.
   */
  private DbTest() {
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
      databaseTypes = Sets.newHashSet(DbDialectUtils.getAvailableDatabaseTypes());
    } else {
      if (DbDialectUtils.getAvailableDatabaseTypes().contains(databaseType) == false) {
        throw new IllegalArgumentException("Unknown database: " + databaseType);
      }
      databaseTypes = Sets.newHashSet(databaseType);
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
    return DbDialectUtils.getSupportedDbDialect(dbType);
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
