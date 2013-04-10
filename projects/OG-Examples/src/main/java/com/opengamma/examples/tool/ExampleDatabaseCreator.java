/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.test.DbTool;

/**
 * Tool class that creates and initializes the example database.
 */
@Scriptable
public class ExampleDatabaseCreator {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabaseCreator.class);

  /** Examples dir. */
  private static final File EXAMPLES_DIR = new File(System.getProperty("user.dir"));
  /** Schema. */
  private static final String CONFIG_FILE = "toolcontext/toolcontext-example.properties";
  /** Database URL. */
  private static final String KEY_URL = "db.standard.url";
  /** Database user name. */
  private static final String KEY_USER_NAME = "db.standard.username";
  /** Database password. */
  private static final String KEY_PASSWORD = "db.standard.password";
  /** Catalog. */
  private static final String CATALOG = "og-financial";
  /** Script files. */
  private static final File SCRIPT_ZIP_PATH = new File(EXAMPLES_DIR, "lib/sql/com.opengamma/og-masterdb");
  /** Script expansion. */
  private static final File SCRIPT_INSTALL_DIR = new File(EXAMPLES_DIR, "temp/" + ExampleDatabaseCreator.class.getSimpleName());

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   *
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.warn("Creating example database");
    try {
      new ExampleDatabaseCreator().run(CONFIG_FILE);
      System.exit(0);
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
      System.exit(1);
    }
  }

  //-------------------------------------------------------------------------
  private void run(String configFile) throws Exception {
    ClassPathResource res = new ClassPathResource(configFile, ClassUtils.getDefaultClassLoader());
    Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    }
    String url = Objects.requireNonNull(props.getProperty(KEY_URL));
    String user = props.getProperty(KEY_USER_NAME, "");
    String password = props.getProperty(KEY_PASSWORD, "");
    
    // find the scripts
    createSQLScripts();
    
    // create main database
    DbTool dbTool = new DbTool();
    dbTool.setJdbcUrl(url);
    dbTool.setUser(user);
    dbTool.setPassword(password);
    dbTool.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbTool.execute();
    s_logger.warn("Main database created");
    
    // create user database
    DbTool dbToolUser = new DbTool();
    dbToolUser.setJdbcUrl(url);
    dbToolUser.setUser(user);
    dbToolUser.setPassword(password);
    dbToolUser.setJdbcUrl("jdbc:hsqldb:file:/temp/hsqldb/og-fin-user");
    dbToolUser.setCatalog(CATALOG);  // ignored, as it is parsed from the url
    dbToolUser.setCreate(true);
    dbToolUser.setDrop(true);
    dbToolUser.setCreateTables(true);
    dbToolUser.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbToolUser.execute();
    s_logger.warn("User database created");
    
    // populate the database
    new ExampleDatabasePopulator().run("classpath:" + configFile, ToolContext.class);
    s_logger.warn("Populated database");
  }

  private static void createSQLScripts() throws IOException {
    cleanScriptDir();
    for (File file : (Collection<File>) FileUtils.listFiles(SCRIPT_ZIP_PATH, new String[] {"zip"}, false)) {
      ZipUtils.unzipArchive(file, SCRIPT_INSTALL_DIR);
    }
  }

  private static void cleanScriptDir() {
    if (SCRIPT_INSTALL_DIR.exists()) {
      FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
    }
  }

}
