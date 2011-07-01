/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;

import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;

/**
 * Tests {@link MarketDataProviderWithOverride}
 */
@Test
public class MarketDataProviderWithOverrideTest {

  public void testSubscriptionFailure() throws InterruptedException {
    MockMarketDataProvider p1 = new MockMarketDataProvider("p1", true, 1);
    MockMarketDataProvider p2 = new MockMarketDataProvider("p2", false, 1);
    MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(p1, p2);
    MarketDataListener listener = mock(MarketDataListener.class);
    provider.addListener(listener);
    
    ValueRequirement req = getRequirement(1);
    
    provider.subscribe(UserPrincipal.getLocalUser(), req);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();
    
    verify(listener).subscriptionFailed(req, "p2");
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionSucceeded(Mockito.<ValueRequirement>anyObject());
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueRequirement>anyObject(), Mockito.anyString());
  }
  
  public void testSubscriptionSuccess() throws InterruptedException {
    MockMarketDataProvider p1 = new MockMarketDataProvider("p1", true, 1);
    MockMarketDataProvider p2 = new MockMarketDataProvider("p2", true, 1);
    MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(p1, p2);
    MarketDataListener listener = mock(MarketDataListener.class);
    provider.addListener(listener);
    
    ValueRequirement req = getRequirement(1);
    
    provider.subscribe(UserPrincipal.getLocalUser(), req);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();
    
    verify(listener).subscriptionSucceeded(req);
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionSucceeded(Mockito.<ValueRequirement>anyObject());
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueRequirement>anyObject(), Mockito.anyString());
    
    p1.valueChanged(Collections.singleton(req));
    p2.valueChanged(Collections.singleton(req));
    verify(listener, VerificationModeFactory.times(2)).valueChanged(req);
  }
  
  public void testSnapshotNoOverrides() throws InterruptedException {
    ValueRequirement req1 = getRequirement(1);
    ValueRequirement req2 = getRequirement(2);
    
    MockMarketDataProvider overrideProvider = new MockMarketDataProvider("p1", true, 1);
    overrideProvider.put(req1, "value1");
    MockMarketDataProvider underlyingProvider = new MockMarketDataProvider("p2", true, 1);
    underlyingProvider.put(req2, "value2");
    MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(underlyingProvider, overrideProvider);
    
    MarketDataSnapshot snapshot = provider.snapshot(null);
    assertEquals(1, overrideProvider.getAndResetSnapshotCount());
    assertEquals(1, underlyingProvider.getAndResetSnapshotCount());
    
    assertEquals("value1", snapshot.query(req1));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(0, underlyingProvider.getAndResetQueryCount());
    
    assertEquals("value2", snapshot.query(req2));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(1, underlyingProvider.getAndResetQueryCount());
    
    ValueRequirement req3 = getRequirement(3);
    assertNull(snapshot.query(req3));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(1, underlyingProvider.getAndResetQueryCount());
    
    assertEquals("value1", snapshot.query(req1));
  }
  
  public void testSnapshotWithOverrides() throws InterruptedException {
    ValueRequirement req1 = getRequirement(1);
    
    MockMarketDataProvider overrideProvider = new MockMarketDataProvider("p1", true, 1);
    overrideProvider.put(req1, "value1");
    MockMarketDataProvider underlyingProvider = new MockMarketDataProvider("p2", true, 1);
    underlyingProvider.put(req1, "value2");
    MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(underlyingProvider, overrideProvider);

    MarketDataSnapshot snapshot = provider.snapshot(null);
    assertEquals(1, overrideProvider.getAndResetSnapshotCount());
    assertEquals(1, underlyingProvider.getAndResetSnapshotCount());
    
    // p1 should override the value in p2
    assertEquals("value1", snapshot.query(req1));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(0, underlyingProvider.getAndResetQueryCount());
  }

  private ValueRequirement getRequirement(int number) {
    return new ValueRequirement("Req-" + number, new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Target")));
  }
    
}
