/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;

import net.sf.ehcache.CacheManager;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = {TestGroup.INTEGRATION, "ehcache"})
public class ReconnectManagerTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Test
  public void reconnection() throws Exception {
    MockLiveDataServer server = new MockLiveDataServer(ExternalScheme.of("BLOOMBERG_BUID"), _cacheManager);
    ReconnectManager manager = new ReconnectManager(server, 20);
    
    try {
      server.subscribe("foo");
      Assert.fail("Not connected yet");
    } catch (RuntimeException e) {
      // ok
    }
    
    manager.start();
    
    assertEquals(0, server.getNumConnections());
    server.connect();
    assertEquals(1, server.getNumConnections());
    
    server.subscribe("foo");
    assertEquals(1, server.getActualSubscriptions().size());
    
    Thread.sleep(50); // shouldn't reconnect
    assertEquals(1, server.getNumConnections());
    assertEquals(0, server.getNumDisconnections());
    
    manager.stop();
    server.disconnect();
    assertEquals(1, server.getNumDisconnections());
    assertEquals(1, server.getNumConnections());
    
    Thread.sleep(1000);
    manager.start(); // should reconnect and reestablish subscriptions
    Thread.sleep(1000);
    
    assertEquals(1, server.getNumDisconnections());
    assertEquals(2, server.getNumConnections());
    assertEquals(2, server.getActualSubscriptions().size());
  }

}
