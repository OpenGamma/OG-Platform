/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 * 
 */
public class TestProperties {

  private static final String DEFAULT_PROPS_FILE_NAME = "tests.properties";
  private static final String DEFAULT_PROPS_DIR = "../../common/"; // OG-Platform/common/

  private static Properties _props = null;
  
  public static synchronized void setBaseDir(String dir) {
    if (_props != null) {
      throw new IllegalStateException("Properties already loaded");
    }
  }
  
  public static synchronized Properties getTestProperties() {
    if (_props == null) {
      _props = new Properties();
      
      String propsFileName = DEFAULT_PROPS_FILE_NAME;
      String overridePropsFileName = System.getProperty("test.properties"); // passed in by Ant
      if (overridePropsFileName != null) {
        propsFileName = overridePropsFileName;
        System.err.println("Using test.properties from system property: " + propsFileName);
      } else {
        System.err.println("Using default test.properties file name: " + propsFileName);
      }
      String testPropsDir = DEFAULT_PROPS_DIR;
      String overridePropsDir = System.getProperty("test.properties.dir"); // passed in by Ant
      if (overridePropsDir != null) {
        testPropsDir = overridePropsDir;
        System.err.println("Using test.properties.dir from system property: " + testPropsDir);
      } else {
        System.err.println("Using default test.properties.dir: " + testPropsDir);
      }
      
      File file = new File(testPropsDir, propsFileName);
      try {
        System.err.println("Reading test properties from " + file.getCanonicalPath());
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("Couldn't get canonical path of file " + file, e);
      }
      try {
        FileInputStream fis = new FileInputStream(file);
        _props.load(fis);
        fis.close();
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("Could not read " + propsFileName, e);
      }
    }
    return _props;
  }

  public static Collection<String> getAllSupportedDatabaseTypes() {
    return Arrays.asList(new String[] { "hsqldb", "postgres"});
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
    String dbHost = getTestProperties().getProperty(dbHostProperty);
    if (dbHost == null) {
      throw new OpenGammaRuntimeException("Property " + dbHostProperty
          + " not found");
    }
    return dbHost;
  }

  public static String getDbUsername(String databaseType) {
    String userProperty = databaseType + ".jdbc.username";
    String user = getTestProperties().getProperty(userProperty);
    if (user == null) {
      throw new OpenGammaRuntimeException("Property " + userProperty
          + " not found");
    }
    return user;
  }

  public static String getDbPassword(String databaseType) {
    String passwordProperty = databaseType + ".jdbc.password";
    String password = getTestProperties().getProperty(passwordProperty);
    if (password == null) {
      throw new OpenGammaRuntimeException("Property " + passwordProperty
          + " not found");
    }
    return password;
  }

  public static DbTool getDbTool(String databaseType) {
    String dbHost = getDbHost(databaseType);
    String user = getDbUsername(databaseType);
    String password = getDbPassword(databaseType);
    
    DbTool dbtool = new DbTool(dbHost, user, password);
    dbtool.initialize();
    return dbtool;
  }
}
