/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;


import static org.testng.Assert.assertEquals;

import java.util.Timer;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.livedata.client.Heartbeater;
import com.opengamma.livedata.client.ValueDistributor;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT_SLOW})
public class ExpirationManagerTest {

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
  public void expirationWithHeartbeatSendingClient() throws InterruptedException {
    ExternalScheme identificationDomain = ExternalScheme.of("BbgId");

    MockLiveDataServer dataServer = new MockLiveDataServer(identificationDomain, _cacheManager);
    dataServer.connect();
    ExpirationManager expirationManager = dataServer.getExpirationManager();
    // Set expiration timeout artificially low
    expirationManager.setTimeoutExtension(300);
    HeartbeatReceiver receiver = new HeartbeatReceiver(expirationManager);
    DirectInvocationByteArrayMessageSender conduit = new DirectInvocationByteArrayMessageSender(receiver);
    ValueDistributor valueDistributor = new ValueDistributor();
    Timer t = new Timer("HeartbeatConduitTest");
    new Heartbeater(valueDistributor, new HeartbeatSender(conduit, OpenGammaFudgeContext.getInstance()), t, 100);

    // subscribe on the client side - starts sending heartbeats
    LiveDataSpecification subscription = new LiveDataSpecification(
        dataServer.getDefaultNormalizationRuleSetId(),
        ExternalId.of(identificationDomain, "USSw5 Curncy"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    valueDistributor.addListener(subscription, listener);

    // subscribe on the server side
    dataServer.subscribe(subscription, false);

    // Send a couple of heartbeats
    Thread.sleep(300);

    // Stop sending heartbeats
    valueDistributor.removeListener(subscription, listener);

    // Wait for expiry
    Thread.sleep(500);

    expirationManager.housekeep(dataServer);

    assertEquals(dataServer.getActualSubscriptions().size(), 1);
    assertEquals(dataServer.getActualUnsubscriptions().size(), 1);
    assertEquals(dataServer.getActualSubscriptions().get(0), subscription.getIdentifier(identificationDomain));
    assertEquals(dataServer.getActualSubscriptions().get(0), subscription.getIdentifier(identificationDomain));
  }

  public void expirationWithClientThatDoesNotSendHeartbeats() throws InterruptedException {
    ExternalScheme identificationDomain = ExternalScheme.of("BbgId");

    MockLiveDataServer dataServer = new MockLiveDataServer(identificationDomain, _cacheManager);
    dataServer.connect();
    ExpirationManager expirationManager = dataServer.getExpirationManager();

    // Set expiration timeout artificially low
    expirationManager.setTimeoutExtension(50);

    // subscribe on the server side
    LiveDataSpecification subscription = new LiveDataSpecification(
        dataServer.getDefaultNormalizationRuleSetId(),
        ExternalId.of(identificationDomain, "USSw5 Curncy"));
    dataServer.subscribe("USSw5 Curncy");

    assertEquals(dataServer.getActualSubscriptions().size(), 1);
    assertEquals(dataServer.getActualSubscriptions().get(0), subscription.getIdentifier(identificationDomain));

    // Wait for expiry
    Thread.sleep(200);

    expirationManager.housekeep(dataServer);

    assertEquals(dataServer.getActualUnsubscriptions().size(), 1);
    assertEquals(dataServer.getActualUnsubscriptions().get(0), subscription.getIdentifier(identificationDomain));
  }

  public void heartbeatWhenNoSubscriptionCreatesNewSubscription() throws InterruptedException {

    ExternalScheme identificationDomain = ExternalScheme.of("BbgId");

    MockLiveDataServer dataServer = new MockLiveDataServer(identificationDomain, _cacheManager);
    dataServer.connect();
    ExpirationManager expirationManager = dataServer.getExpirationManager();
    // Set expiration timeout artificially low
    expirationManager.setTimeoutExtension(300);

    HeartbeatReceiver receiver = new HeartbeatReceiver(expirationManager);
    DirectInvocationByteArrayMessageSender conduit = new DirectInvocationByteArrayMessageSender(receiver);
    ValueDistributor valueDistributor = new ValueDistributor();
    Timer t = new Timer("HeartbeatConduitTest");
    new Heartbeater(valueDistributor, new HeartbeatSender(conduit, OpenGammaFudgeContext.getInstance()), t, 100);

    // subscribe on the client side - starts sending heartbeats
    LiveDataSpecification subscription = new LiveDataSpecification(
        dataServer.getDefaultNormalizationRuleSetId(),
        ExternalId.of(identificationDomain, "USSw5 Curncy"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    valueDistributor.addListener(subscription, listener);

    assertEquals(dataServer.getSubscriptions().size(), 0);

    // subscribe on the server side
    dataServer.subscribe(subscription, false);

    // Send a couple of heartbeats
    Thread.sleep(300);

    assertEquals(dataServer.getSubscriptions().size(), 1);

    // Unsubscribe the server
    dataServer.unsubscribe(subscription.getIdentifier(identificationDomain));
    assertEquals(dataServer.getSubscriptions().size(), 0);

    // Send a couple of heartbeats
    Thread.sleep(300);

    expirationManager.housekeep(dataServer);

    // Now we should be subscribed again due to heartbeating
    assertEquals(dataServer.getSubscriptions().size(), 1);

    // Check the recording of what happened:
    // we subscribed (via client), unsubscribed (at the server) and subscribed again (due to heartbeating)
    assertEquals(dataServer.getActualSubscriptions().size(), 2);
    assertEquals(dataServer.getActualUnsubscriptions().size(), 1);
    assertEquals(dataServer.getActualSubscriptions().get(0), subscription.getIdentifier(identificationDomain));
    assertEquals(dataServer.getActualSubscriptions().get(1), subscription.getIdentifier(identificationDomain));
    assertEquals(dataServer.getActualUnsubscriptions().get(0), subscription.getIdentifier(identificationDomain));
  }
}
