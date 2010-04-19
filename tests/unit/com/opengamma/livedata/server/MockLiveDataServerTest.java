/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author pietari
 */
public class MockLiveDataServerTest {
  
  private IdentificationScheme _domain;
  private MockLiveDataServer _server;
  
  @Before
  public void setUp() {
    _domain = new IdentificationScheme("test");
    _server = new MockLiveDataServer(_domain);
    _server.connect();
  }
  
  @Test
  public void getSubscription() {
    _server.subscribe("nonpersistent", false);
    _server.subscribe("persistent", true);
    
    Subscription nonpersistent = _server.getSubscription("nonpersistent"); 
    Subscription persistent = _server.getSubscription("persistent");
    
    assertNotNull(nonpersistent);
    assertNotNull(persistent);
    
    assertFalse(nonpersistent.isPersistent());
    assertTrue(persistent.isPersistent());
    
    assertNotNull(nonpersistent.getExpiry());
    assertNull(persistent.getExpiry());
    
    assertEquals("nonpersistent", nonpersistent.getSecurityUniqueId());
    assertEquals("persistent", persistent.getSecurityUniqueId());
    
    Subscription nonpersistentB = _server.getSubscription("nonpersistent"); 
    Subscription persistentB = _server.getSubscription("persistent");
    
    assertSame(nonpersistent, nonpersistentB);
    assertSame(persistent, persistentB);
  }

  @Test
  public void subscribeUnsubscribeA() {
    _server.subscribe("nonpersistent", false);
    _server.subscribe("persistent", true);
    
    assertTrue(_server.unsubscribe("nonpersistent"));
    assertTrue(_server.unsubscribe("persistent"));
  }
  
  @Test
  public void subscribeUnsubscribeB() {
    _server.subscribe("nonpersistent", false);
    _server.subscribe("persistent", true);
    
    Subscription nonpersistent = _server.getSubscription("nonpersistent"); 
    Subscription persistent = _server.getSubscription("persistent");
    
    assertTrue(_server.unsubscribe(nonpersistent));
    assertFalse(_server.unsubscribe(persistent)); // this version of unsubscribe will not do anything to persistent subscriptions 
  }
  
}
