/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static com.opengamma.integration.regression.PropertiesUtils.createProperties;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.db.tool.DbTool;

/**
 *
 */
public class EmptyDatabaseCreator {

  private static final String s_managerInclude = "MANAGER.INCLUDE";

  private static final Logger s_logger = LoggerFactory.getLogger(EmptyDatabaseCreator.class);

  /** Shared database URL. */
  public static final String KEY_SHARED_URL = "db.standard.url";
  /** Shared database user name. */
  public static final String KEY_SHARED_USER_NAME = "db.standard.username";
  /** Shared database password. */
  public static final String KEY_SHARED_PASSWORD = "db.standard.password";
  /** Temporary user database URL. */
  public static final String KEY_USERFINANCIAL_URL = "db.userfinancial.url";
  /** Temporary user database user name. */
  public static final String KEY_USERFINANCIAL_USER_NAME = "db.userfinancial.username";
  /** Temporary user database password. */
  public static final String KEY_USERFINANCIAL_PASSWORD = "db.userfinancial.password";
  /** Catalog. */
  private static final String CATALOG = "og-financial";

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      throw new IllegalArgumentException("Argument required specifying configuration file");
    }
    EmptyDatabaseCreator.createDatabases(createProperties(args[0]));
  }

  public static void createForConfig(String configFile) {
    
    Properties allProperties = createProperties(configFile);
    
    //loosely adds support for includes:
    for (Properties lastProperties = allProperties; lastProperties.containsKey(s_managerInclude); ) {
      Properties properties = createProperties(lastProperties.getProperty(s_managerInclude));
      allProperties.putAll(properties);
      lastProperties = properties;
    };
    
    createDatabases(allProperties);
  }
  

  public static void createDatabases(Properties props) {
    // create main database
    s_logger.info("Creating main database using properties {}", props);
    DbTool dbTool = new DbTool();
    dbTool.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_SHARED_URL)));
    dbTool.setUser(props.getProperty(KEY_SHARED_USER_NAME, ""));
    dbTool.setPassword(props.getProperty(KEY_SHARED_PASSWORD, ""));
    dbTool.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.execute();

    // create user database
    s_logger.info("Creating user database using properties {}", props);
    DbTool dbToolUser = new DbTool();
    dbToolUser.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_USERFINANCIAL_URL)));
    dbToolUser.setUser(props.getProperty(KEY_USERFINANCIAL_USER_NAME, ""));
    dbToolUser.setPassword(props.getProperty(KEY_USERFINANCIAL_PASSWORD, ""));
    dbToolUser.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbToolUser.setCreate(true);
    dbToolUser.setDrop(true);
    dbToolUser.setCreateTables(true);
    dbToolUser.execute();
  }
}
