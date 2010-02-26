/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Timer;

import org.fudgemsg.FudgeContext;
import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.CollectingLiveDataListener;
import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.livedata.LiveDataSubscriptionRequest;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.livedata.client.ValueDistributor;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;

/**
 * 
 *
 * @author pietari
 */
public class ActiveSecurityPublicationManagerTest {
  
  @Test
  public void expiration() throws InterruptedException {
    MockLiveDataServer dataServer = new MockLiveDataServer();
    ActiveSecurityPublicationManager pubManager = new ActiveSecurityPublicationManager(dataServer, 100, 500);
    HeartbeatReceiver receiver = new HeartbeatReceiver(pubManager);
    DirectInvocationByteArrayMessageSender conduit = new DirectInvocationByteArrayMessageSender(receiver);
    ValueDistributor valueDistributor = new ValueDistributor();
    Timer t = new Timer("HeartbeatConduitTest");
    HeartbeatSender sender = new HeartbeatSender(conduit, valueDistributor, new FudgeContext(), t, 100);
    
    // subscribe on the client side - starts sending heartbeats
    LiveDataSpecificationImpl subscription = new LiveDataSpecificationImpl(new DomainSpecificIdentifier(new IdentificationDomain("BbgId"), "USSw5 Curncy"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    valueDistributor.addListener(subscription, listener);
    
    // subscribe on the server side
    dataServer.subscriptionRequestMade(new LiveDataSubscriptionRequest("test", Collections.singleton(subscription)));
    
    // Send a couple of heartbeats
    Thread.sleep(300);
    
    // Stop sending heartbeats
    valueDistributor.removeListener(subscription, listener);
    
    // Wait for expiry
    Thread.sleep(1000);
    
    assertEquals(1, dataServer.getSubscriptions().size());
    assertEquals(1, dataServer.getUnsubscriptions().size());
    assertEquals(subscription, dataServer.getSubscriptions().get(0));
    assertEquals(subscription, dataServer.getUnsubscriptions().get(0));
    
    t.cancel();
  }

}
