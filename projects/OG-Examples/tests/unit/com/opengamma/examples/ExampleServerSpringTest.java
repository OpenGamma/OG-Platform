/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples;

import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractSpringContextValidationTestNG;
import com.opengamma.util.test.DBTool;


public class ExampleServerSpringTest extends AbstractSpringContextValidationTestNG {
  
  @BeforeMethod
  public void setUp() throws IOException {
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
    dbTool.setDbScriptDir(System.getProperty("user.dir"));
    dbTool.execute();
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
