/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.tool;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.scripts.Scriptable;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.tool.DbTool;

/** Tool class that creates and initializes the example database. */
@Scriptable
public class ExampleDatabaseChecker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabaseChecker.class);

  /** Shared database URL. */
  private static final String KEY_SHARED_URL = "db.standard.url";
  /** Shared database user name. */
  private static final String KEY_SHARED_USER_NAME = "db.standard.username";
  /** Shared database password. */
  private static final String KEY_SHARED_PASSWORD = "db.standard.password";
  /** Temporary user database URL. */
  private static final String KEY_USERFINANCIAL_URL = "db.userfinancial.url";
  /** Temporary user database user name. */
  private static final String KEY_USERFINANCIAL_USER_NAME = "db.userfinancial.username";
  /** Temporary user database password. */
  private static final String KEY_USERFINANCIAL_PASSWORD = "db.userfinancial.password";
  /** Catalog. */
  private static final String CATALOG = "og-financial";

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool. No arguments are needed.
   * <p/>
   * If the command line is empty, the "development" configuration file is started. This file is intended for use with an IDE and a checked out source code tree. It relies on the OG-Web directory
   * being relative to Examples-Simulated in the file system as per a standard checkout of OG-Platform.
   * 
   * @param args the arguments, unused
   */
  public static void main(String[] args) throws Exception {
    try {
      ExampleDatabaseChecker.run(args);
      System.exit(0);
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static List<String> run(String[] args) throws Exception {
    if (args.length == 0) {
      // if no command line arguments, then use default arguments suitable for development in an IDE
      // the first argument is for verbose startup, to aid understanding
      // the second argument defines the start of a chain of properties files providing the configuration
      args = new String[] {"classpath:/toolcontext/toolcontext-examplesbloomberg.properties" };
    }
    return ExampleDatabaseChecker.run(args[0]);
  }

  public static List<String> run(String configFile) throws Exception {
    Resource res = ResourceUtils.createResource(configFile);
    Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    }

    s_logger.info("Checking main database...");
    DbTool dbTool = new DbTool();
    dbTool.setJdbcUrl(Objects.requireNonNull(props.getProperty(KEY_SHARED_URL)));
    dbTool.setUser(props.getProperty(KEY_SHARED_USER_NAME, ""));
    dbTool.setPassword(props.getProperty(KEY_SHARED_PASSWORD, ""));
    dbTool.setCatalog(CATALOG); // ignored, as it is parsed from the url
    List<String> tables = dbTool.listTables();
    s_logger.debug("url: " + Objects.requireNonNull(props.getProperty(KEY_SHARED_URL)));
    s_logger.info(tables.isEmpty() ? "Main database not found" : "Found existing database");
    return tables;
  }

}
