/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;

import java.util.Timer;

import org.fudgemsg.FudgeContext;
import org.junit.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.livedata.client.ValueDistributor;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;

/**
 * Test ExpirationManager.
 */
public class ExpirationManagerTest {
  
  @Test
  public void expirationWithHeartbeatSendingClient() throws InterruptedException {
    IdentificationScheme identificationDomain = IdentificationScheme.of("BbgId");
    
    MockLiveDataServer dataServer = new MockLiveDataServer(identificationDomain);
    dataServer.connect();
    ExpirationManager expirationManager = new ExpirationManager(dataServer, 100, 500);
    HeartbeatReceiver receiver = new HeartbeatReceiver(expirationManager);
    DirectInvocationByteArrayMessageSender conduit = new DirectInvocationByteArrayMessageSender(receiver);
    ValueDistributor valueDistributor = new ValueDistributor();
    Timer t = new Timer("HeartbeatConduitTest");
    new HeartbeatSender(conduit, valueDistributor, new FudgeContext(), t, 100);
    
    // subscribe on the client side - starts sending heartbeats
    LiveDataSpecification subscription = new LiveDataSpecification(
        dataServer.getDefaultNormalizationRuleSetId(),
        Identifier.of(identificationDomain, "USSw5 Curncy"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    valueDistributor.addListener(subscription, listener);
    
    // subscribe on the server side
    dataServer.subscribe(subscription, false);
    
    // Send a couple of heartbeats
    Thread.sleep(300);
    
    // Stop sending heartbeats
    valueDistributor.removeListener(subscription, listener);
    
    // Wait for expiry
    Thread.sleep(150);
    
    expirationManager.expirationCheck();
    
    assertEquals(1, dataServer.getActualSubscriptions().size());
    assertEquals(1, dataServer.getActualUnsubscriptions().size());
    assertEquals(subscription.getIdentifier(identificationDomain), dataServer.getActualSubscriptions().get(0));
    assertEquals(subscription.getIdentifier(identificationDomain), dataServer.getActualUnsubscriptions().get(0));
  }
  
  @Test
  public void expirationWithClientThatDoesNotSendHeartbeats() throws InterruptedException {
    IdentificationScheme identificationDomain = IdentificationScheme.of("BbgId");
    
    MockLiveDataServer dataServer = new MockLiveDataServer(identificationDomain);
    dataServer.connect();
    ExpirationManager expirationManager = new ExpirationManager(dataServer, 100, 500);
    
    // subscribe on the server side
    LiveDataSpecification subscription = new LiveDataSpecification(
        dataServer.getDefaultNormalizationRuleSetId(),
        Identifier.of(identificationDomain, "USSw5 Curncy"));
    dataServer.subscribe("USSw5 Curncy");
    
    assertEquals(1, dataServer.getActualSubscriptions().size());
    assertEquals(subscription.getIdentifier(identificationDomain), dataServer.getActualSubscriptions().get(0));
    
    // Wait for expiry
    Thread.sleep(150);
    
    expirationManager.expirationCheck();
    
    assertEquals(1, dataServer.getActualUnsubscriptions().size());
    assertEquals(subscription.getIdentifier(identificationDomain), dataServer.getActualUnsubscriptions().get(0));
  }

}
