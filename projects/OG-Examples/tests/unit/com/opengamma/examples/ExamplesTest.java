/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.rest.RemoteComponentServer;

/**
 * Test.
 */
@Test(singleThreaded = true)
public class ExamplesTest {

  private static final String CONFIG_RESOURCE_LOCATION = "classpath:fullstack/example-test.properties";

  @BeforeMethod
  public void setUp() throws IOException {
    DBTestUtils.createTestHsqlDB(CONFIG_RESOURCE_LOCATION);
  }

  @AfterMethod
  public void runAfter() throws IOException {
    DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
  }

  //-------------------------------------------------------------------------
  public void test() throws Exception {
    ComponentManager manager = new ComponentManager("test");
    manager.start(CONFIG_RESOURCE_LOCATION);
    
    RemoteComponentServer remoteServer = new RemoteComponentServer(URI.create("http://localhost:" + getJettyPort() + "/jax"));
    assertTrue(remoteServer.getComponentServer().getComponentInfos().size() > 0);
    
    manager.getRepository().stop();
  }

  private String getJettyPort() throws IOException {
    return DBTestUtils.getJettyPort(CONFIG_RESOURCE_LOCATION);
  }

}
