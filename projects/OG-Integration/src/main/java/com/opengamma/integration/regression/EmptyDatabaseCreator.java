/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.tool.DbTool;

/**
 *
 */
/* package */ class EmptyDatabaseCreator {

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

  private static Properties createProperties(String configFile) {
    Resource res = ResourceUtils.createResource(configFile);
    Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to load config", e);
    }
    return props;
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
