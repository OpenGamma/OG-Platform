/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.mongo.MongoConnector;
import com.opengamma.util.test.MongoTestUtils;

/**
 * Test.
 */
@Test(groups = {"ehcache"})
public class MongoDBPersistentSubscriptionManagerTest {

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
  @Test(enabled=false)
  public void persistentSubscriptionManagement() {
    ExternalScheme idScheme = ExternalScheme.of("TestDomain");
    MockLiveDataServer server = new MockLiveDataServer(idScheme, _cacheManager);
    server.connect();
    
    MongoConnector connector = MongoTestUtils.makeTestConnector(MongoDBPersistentSubscriptionManagerTest.class.getSimpleName(), true);
    MongoDBPersistentSubscriptionManager manager = new MongoDBPersistentSubscriptionManager(server, connector);
    manager.clean();
    
    assertTrue(manager.getPersistentSubscriptions().isEmpty());

    manager.refresh();
    assertTrue(manager.getPersistentSubscriptions().isEmpty());  // test setup will have cleared the db, nothing there initially
    manager.save();
    
    server.subscribe("testsub1", true);
    server.subscribe("testsub2", true);
 
    server.subscribe("testsub3", false);
    server.subscribe("testsub4", false);
    server.subscribe("testsub5", false);
    
    manager.save();
    assertEquals(2, manager.getPersistentSubscriptions().size());
    assertTrue(server.getMarketDataDistributor("testsub1").isPersistent());

    manager.refresh();
    assertEquals(2, manager.getPersistentSubscriptions().size());
    assertTrue(server.getMarketDataDistributor("testsub1").isPersistent());
    
    boolean removed = manager.removePersistentSubscription("testsub1");
    assertTrue(removed);
    assertEquals(1, manager.getPersistentSubscriptions().size());
    assertFalse(server.getMarketDataDistributor("testsub1").isPersistent());
    
    assertFalse(manager.removePersistentSubscription("nonexistentsub"));
    
    manager.addPersistentSubscription("testsub6");
    assertEquals(2, manager.getPersistentSubscriptions().size());
    assertTrue(server.getMarketDataDistributor("testsub6").isPersistent());
  }

}
