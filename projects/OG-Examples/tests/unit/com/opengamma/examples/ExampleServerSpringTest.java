/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples;

import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.ZipUtils;
import com.opengamma.util.test.AbstractSpringContextValidationTestNG;
import com.opengamma.util.test.DBTool;


public class ExampleServerSpringTest extends AbstractSpringContextValidationTestNG {
  
  private static final File SCRIPT_ZIP_PATH = new File(System.getProperty("user.dir"), "lib/sql/com.opengamma/og-masterdb");
  private static final File SCRIPT_INSTALL_DIR = new File(System.getProperty("user.dir"), "temp/" + ExampleServerSpringTest.class.getSimpleName());
  
  @BeforeMethod
  public void setUp() throws IOException {
    createSQLScripts();
    Properties props = new Properties();
    props.load(getClass().getResourceAsStream("/demoMasters-test.properties"));
    DBTool dbTool = new DBTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty("opengamma.financial.jdbc.url"));
    dbTool.setUser(props.getProperty("opengamma.financial.jdbc.username"));
    dbTool.setPassword(props.getProperty("opengamma.financial.jdbc.password"));
    dbTool.setDrop(true);
    dbTool.setCreate(true);
    dbTool.setCreateTables(true);
    dbTool.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbTool.execute();
  }
  
  @SuppressWarnings("unchecked")
  private void createSQLScripts() throws IOException {
    cleanUp();
    for (File file : (Collection<File>) FileUtils.listFiles(SCRIPT_ZIP_PATH, new String[] {"zip"}, false)) {
      ZipUtils.unzipArchive(file, SCRIPT_INSTALL_DIR);
    }
  }

  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
    cleanUp();
  }

  private void cleanUp() {
    FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
  }

  @Test(dataProvider = "runModes", dataProviderClass = ExampleServerSpringTest.class)
  public void testJettyServerLoaderBean(final String opengammaPlatformRunmode) {
    loadFileSystemResource(opengammaPlatformRunmode, "config/engine-spring.xml");
    assertContextLoaded();
    assertBeanExists(WebAppContext.class, "webAppContext");
    assertBeanExists(Server.class, "server");
    assertBeanExists(WebAppContext.class, "webAppContext");
    
    WebAppContext webAppContext = getSpringContext().getBean("webAppContext", WebAppContext.class);
    if (!webAppContext.isAvailable()) {
      fail("Web application context not available");
    }
    Server server = getSpringContext().getBean("server", Server.class);
    if (server.isFailed()) {
      fail("Jetty server failed to start");
    }
  }
  
  @DataProvider(name = "runModes")
  public static Object[][] data_runMode() {
    return new Object[][] {
      {"test"},
    };
  }
}
