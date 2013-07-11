/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.tool.DbTool;

/**
 * Test utilities.
 */
public final class DBTestUtils {
 
  private static final String DB_PASSWORD_KEY = "db.standard.password";
  private static final String DB_USERNAME_KEY = "db.standard.username";
  private static final String JDBC_URL_KEY = "db.standard.url";
  private static final String JDBC_URL_KEY_USER = "db.userfinancial.url";
  private static final Logger s_logger = LoggerFactory.getLogger(DBTestUtils.class);
  
  private DBTestUtils() {
  }

  public static void createTestHsqlDB(String configResourceLocation) throws IOException {
    Properties props = loadProperties(configResourceLocation);
    
    DbTool dbTool = new DbTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty(JDBC_URL_KEY));
    dbTool.setUser(props.getProperty(DB_USERNAME_KEY, ""));
    dbTool.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.execute();
    
    if (StringUtils.isNotEmpty(props.getProperty(JDBC_URL_KEY_USER))) {
      DbTool dbTool2 = new DbTool();
      dbTool2.setCatalog("og-financial");
      dbTool2.setJdbcUrl(props.getProperty(JDBC_URL_KEY_USER));
      dbTool2.setUser(props.getProperty(DB_USERNAME_KEY, ""));
      dbTool2.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
      dbTool2.setCreate(true);
      dbTool2.setDrop(true);
      dbTool2.setCreateTables(true);
      dbTool2.execute();
    }
  }
  
  public static Properties loadProperties(String configResourceLocation) throws IOException {
    Resource resource = ResourceUtils.createResource(configResourceLocation);
    Properties props = new Properties();
    props.load(resource.getInputStream());
    
    String nextConfiguration = props.getProperty("MANAGER.NEXT.FILE");
    if (nextConfiguration != null) {
      resource = ResourceUtils.createResource(nextConfiguration);
      Properties parentProps = new Properties();
      parentProps.load(resource.getInputStream());
      for (String key : props.stringPropertyNames()) {
        parentProps.put(key, props.getProperty(key));
      }
      props = parentProps;
    }
    
    for (String key : props.stringPropertyNames()) {
      s_logger.debug("\t{}={}", key, props.getProperty(key));
    }
    
    return props;
  }

  public static void cleanUp(String configResourceLocation) throws IOException {
    dropDatabase(configResourceLocation);
  }
  
  private static void dropDatabase(String configResourceLocation) throws IOException {
    Properties props = loadProperties(configResourceLocation);
    
    DbTool dbTool = new DbTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty(JDBC_URL_KEY));
    dbTool.setUser(props.getProperty(DB_USERNAME_KEY, ""));
    dbTool.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
    dbTool.setDrop(true);
    dbTool.execute();
    
    if (StringUtils.isNotEmpty(props.getProperty(JDBC_URL_KEY_USER))) {
      DbTool dbTool2 = new DbTool();
      dbTool2.setCatalog("og-financial");
      dbTool2.setJdbcUrl(props.getProperty(JDBC_URL_KEY_USER));
      dbTool2.setUser(props.getProperty(DB_USERNAME_KEY, ""));
      dbTool2.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
      dbTool2.setDrop(true);
      dbTool2.execute();
    }
  }

  public static String getJettyPort(String configResourceLocation) throws IOException {
    Properties props = loadProperties(configResourceLocation);
    return props.getProperty("jetty.port");
  }
  
}
