/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * 
 *
 * @author pietari
 */
public class MongoDBPersistentSubscriptionManagerTest {
  
  @Test
  public void persistentSubscriptionManagement() {
    
    IdentificationScheme identificationDomain = IdentificationScheme.of("TestDomain");
    
    MockLiveDataServer server = new MockLiveDataServer(identificationDomain);
    server.connect();
    
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(MongoDBPersistentSubscriptionManagerTest.class.getSimpleName(), true);
    MongoDBPersistentSubscriptionManager manager = new MongoDBPersistentSubscriptionManager(server, settings);
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
