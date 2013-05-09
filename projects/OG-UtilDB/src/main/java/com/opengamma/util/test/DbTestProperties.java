/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Database testing properties.
 */
public class DbTestProperties {

  /**
   * Gets the supported databases for testing.
   * 
   * @return the database types, not null
   */
  public static Collection<String> getAllSupportedDatabaseTypes() {
    return Arrays.asList(new String[] { "hsqldb", "postgres", "sqlserver2008"});    
  }

  /**
   * @return A singleton collection containing the String passed in), except if the type is ALL (case
   *         insensitive), in which case all supported database types are returned.
   */
  public static Collection<String> getDatabaseTypes(String commandLineDbType) {
    ArrayList<String> dbTypes = new ArrayList<String>();
    if (commandLineDbType.trim().equalsIgnoreCase("all")) {
      dbTypes.addAll(getAllSupportedDatabaseTypes());
    } else {
      dbTypes.add(commandLineDbType);
    }
    return dbTypes;
  }

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
