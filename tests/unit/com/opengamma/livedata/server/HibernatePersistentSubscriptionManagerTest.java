/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.test.HibernateTest;

/**
 * 
 *
 * @author pietari
 */
public class HibernatePersistentSubscriptionManagerTest extends HibernateTest {
  
  public HibernatePersistentSubscriptionManagerTest(String databaseType) {
    super(databaseType);
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return HibernatePersistentSubscriptionManager.getHibernateMappingClasses();
  }

  @Test
  public void persistentSubscriptionManagement() {
    
    IdentificationDomain identificationDomain = new IdentificationDomain("TestDomain");
    
    MockLiveDataServer server = new MockLiveDataServer(identificationDomain);
    HibernatePersistentSubscriptionManager manager = new HibernatePersistentSubscriptionManager(server);
    manager.setSessionFactory(getSessionFactory());
    
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
    assertTrue(server.getSubscription("testsub1").isPersistent());

    manager.refresh();
    assertEquals(2, manager.getPersistentSubscriptions().size());
    assertTrue(server.getSubscription("testsub1").isPersistent());
    
    boolean removed = manager.removePersistentSubscription("testsub1");
    assertTrue(removed);
    assertEquals(1, manager.getPersistentSubscriptions().size());
    assertFalse(server.getSubscription("testsub1").isPersistent());
    
    assertFalse(manager.removePersistentSubscription("nonexistentsub"));
    
    manager.addPersistentSubscription("testsub6");
    assertEquals(2, manager.getPersistentSubscriptions().size());
    assertTrue(server.getSubscription("testsub6").isPersistent());
  }
}