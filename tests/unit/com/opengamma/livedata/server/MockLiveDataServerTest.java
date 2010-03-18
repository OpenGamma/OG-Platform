/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.LiveDataSpecificationImpl;

/**
 * 
 *
 * @author pietari
 */
public class MockLiveDataServerTest {
  
  private IdentificationDomain _domain;
  private MockLiveDataServer _server;
  
  @Before
  public void setUp() {
    _domain = new IdentificationDomain("test");
    _server = new MockLiveDataServer(_domain);
  }
  
  @Test
  public void getSubscription() {
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "nonpersistent")), false);
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "persistent")), true);
    
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
    
    Subscription nonpersistentB = _server.getSubscription(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "nonpersistent"))); 
    Subscription persistentB = _server.getSubscription(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "persistent")));
    
    assertSame(nonpersistent, nonpersistentB);
    assertSame(persistent, persistentB);
  }

  @Test
  public void subscribeUnsubscribeA() {
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "nonpersistent")), false);
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "persistent")), true);
    
    assertTrue(_server.unsubscribe("nonpersistent"));
    assertTrue(_server.unsubscribe("persistent"));
  }
  
  @Test
  public void subscribeUnsubscribeB() {
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "nonpersistent")), false);
    _server.subscribe(new LiveDataSpecificationImpl(new DomainSpecificIdentifier(_domain, "persistent")), true);
    
    Subscription nonpersistent = _server.getSubscription("nonpersistent"); 
    Subscription persistent = _server.getSubscription("persistent");
    
    assertTrue(_server.unsubscribe(nonpersistent));
    assertFalse(_server.unsubscribe(persistent)); // this version of unsubscribe will not do anything to persistent subscriptions 
  }
  
}
