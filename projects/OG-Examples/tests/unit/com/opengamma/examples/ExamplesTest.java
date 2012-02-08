/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.RemoteComponentServer;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.test.DbTool;

/**
 * Test.
 */
@Test
public class ExamplesTest {

  private static final File SCRIPT_ZIP_PATH = new File(System.getProperty("user.dir"), "lib/sql/com.opengamma/og-masterdb");
  private static final File SCRIPT_INSTALL_DIR = new File(System.getProperty("user.dir"), "temp/" + ExamplesTest.class.getSimpleName());
  private String _jettyPort;

  @BeforeMethod
  public void setUp() throws IOException {
    createSQLScripts();
    Resource resource = ComponentManager.createResource("classpath:fullstack/fullstack-example-test.properties");
    Properties props = new Properties();
    props.load(resource.getInputStream());
    _jettyPort = props.getProperty("jetty.port");
    
    DbTool dbTool = new DbTool();
    dbTool.setCatalog("og-financial");
    dbTool.setJdbcUrl(props.getProperty("db.standard.url"));
    dbTool.setUser("");
    dbTool.setPassword("");
    dbTool.setDrop(true);
    dbTool.setCreate(true);
    dbTool.setCreateTables(true);
    dbTool.setDbScriptDir(SCRIPT_INSTALL_DIR.getAbsolutePath());
    dbTool.execute();
  }

  private void createSQLScripts() throws IOException {
    cleanUp();
    for (File file : (Collection<File>) FileUtils.listFiles(SCRIPT_ZIP_PATH, new String[] {"zip"}, false)) {
      ZipUtils.unzipArchive(file, SCRIPT_INSTALL_DIR);
    }
  }

  @AfterMethod
  public void runAfter() {
    cleanUp();
  }

  private void cleanUp() {
    FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
  }

  //-------------------------------------------------------------------------
  public void test() {
    ComponentManager manager = new ComponentManager();
    manager.start("classpath:fullstack/fullstack-example-test.properties");
    
    RemoteComponentServer remoteServer = new RemoteComponentServer(URI.create("http://localhost:" + _jettyPort + "/jax"));
    assertTrue(remoteServer.getComponentServer().getComponentInfos().size() > 0);
    
    manager.getRepository().stop();
  }

}
