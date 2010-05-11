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

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.livedata.normalization.StandardRules;

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
  
  @Test
  public void subscribeUnsubscribeC() {
    UserPrincipal user = new UserPrincipal("mark", "1.1.1.1");
    
    LiveDataSpecification requestedSpec = new LiveDataSpecification(
        StandardRules.getNoNormalization().getId(), 
        new Identifier(_domain, "testsub"));
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.NON_PERSISTENT, 
        Collections.singleton(requestedSpec));
    
    LiveDataSubscriptionResponseMsg response = _server.subscriptionRequestMade(request);
    
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe("testsub"));
    
    response = _server.subscriptionRequestMade(request);
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe("testsub"));
  }

  private void checkResponse(UserPrincipal user, LiveDataSpecification requestedSpec,
      LiveDataSubscriptionResponseMsg response) {
    assertEquals(user, response.getRequestingUser());
    assertEquals(1, response.getResponses().size());
    assertEquals(requestedSpec, response.getResponses().get(0).getRequestedSpecification());
    assertEquals(requestedSpec, response.getResponses().get(0).getFullyQualifiedSpecification()); // mock server does not modify spec
    assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getResponses().get(0).getSubscriptionResult());
    assertEquals(null, response.getResponses().get(0).getSnapshot()); 
    assertEquals(requestedSpec.getIdentifiers().toString(), response.getResponses().get(0).getTickDistributionSpecification());
    assertEquals(null, response.getResponses().get(0).getUserMessage());
  }
  
}
