package com.opengamma.examples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.component.ComponentManager;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.test.DbTool;

public final class DBTestUtils {
 
  private static final String DB_PASSWORD_KEY = "db.standard.password";
  private static final String DB_USERNAME_KEY = "db.standard.username";
  private static final String JDBC_URL_KEY = "db.standard.url";
  private static final Logger s_logger = LoggerFactory.getLogger(DBTestUtils.class);
  
  private static final File SCRIPT_ZIP_PATH = new File(System.getProperty("user.dir"), "lib/sql/com.opengamma/og-masterdb");
  private static final File SCRIPT_INSTALL_DIR = new File(System.getProperty("user.dir"), "temp/" + ExamplesTest.class.getSimpleName());
  
  private DBTestUtils() {
  }

  public static void createHsqlDB(String configResourceLocation) throws IOException {
    createSQLScripts();
    Properties props = loadProperties(configResourceLocation);
    
    DbTool dbTool = new DbTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty(JDBC_URL_KEY));
    dbTool.setUser(props.getProperty(DB_USERNAME_KEY, ""));
    dbTool.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
    dbTool.setCreate(true);
    dbTool.setDrop(true);
    dbTool.setCreateTables(true);
    dbTool.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbTool.execute();
  }
  
  public static Properties loadProperties(String configResourceLocation) throws IOException {
    Resource resource = ComponentManager.createResource(configResourceLocation);
    Properties props = new Properties();
    props.load(resource.getInputStream());
    
    String nextConfiguration = props.getProperty("MANAGER.NEXT.FILE");
    if (nextConfiguration != null) {
      resource = ComponentManager.createResource(nextConfiguration);
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

  public static void cleanUp(String configResourceLocation) throws IOException {
    dropDatabase(configResourceLocation);
    cleanScriptDir();
  }
  
  private static void dropDatabase(String configResourceLocation) throws IOException {
    Properties props = loadProperties(configResourceLocation);
    DbTool dbTool = new DbTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty(JDBC_URL_KEY));
    dbTool.setUser(props.getProperty(DB_USERNAME_KEY, ""));
    dbTool.setPassword(props.getProperty(DB_PASSWORD_KEY, ""));
    dbTool.setDrop(true);
    dbTool.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbTool.execute();
  }

  public static String getJettyPort(String configResourceLocation) throws IOException {
    Properties props = loadProperties(configResourceLocation);
    return props.getProperty("jetty.port");
  }
  
}
