/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Tests CombiningLiveDataSnapshotProvider
 */
public class CombiningLiveDataSnapshotProviderTest {

  @Test
  public void testSubscriptionFailure() throws InterruptedException {
    MockLiveDataSnapshotProvider p1 = new MockLiveDataSnapshotProvider("p1", true, 1);
    MockLiveDataSnapshotProvider p2 = new MockLiveDataSnapshotProvider("p2", false, 1);
    CombiningLiveDataSnapshotProvider provider = new CombiningLiveDataSnapshotProvider(Arrays.<LiveDataSnapshotProvider>asList(p1, p2));
    LiveDataSnapshotListener listener = mock(LiveDataSnapshotListener.class);
    provider.addListener(listener);
    
    ValueRequirement req = getRequirement(1);
    
    provider.addSubscription(UserPrincipal.getLocalUser(), req);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();
    
    verify(listener).subscriptionFailed(req, "p2");
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionSucceeded(Mockito.<ValueRequirement>anyObject());
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueRequirement>anyObject(), Mockito.anyString());
  }
  
  @Test
  public void testSubscriptionSuccess() throws InterruptedException {
    MockLiveDataSnapshotProvider p1 = new MockLiveDataSnapshotProvider("p1", true, 1);
    MockLiveDataSnapshotProvider p2 = new MockLiveDataSnapshotProvider("p2", true, 1);
    CombiningLiveDataSnapshotProvider provider = new CombiningLiveDataSnapshotProvider(Arrays.<LiveDataSnapshotProvider>asList(p1, p2));
    LiveDataSnapshotListener listener = mock(LiveDataSnapshotListener.class);
    provider.addListener(listener);
    
    ValueRequirement req = getRequirement(1);
    
    provider.addSubscription(UserPrincipal.getLocalUser(), req);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();
    
    verify(listener).subscriptionSucceeded(req);
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionSucceeded(Mockito.<ValueRequirement>anyObject());
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueRequirement>anyObject(), Mockito.anyString());
    
    p1.valueChanged(Collections.singleton(req));
    p2.valueChanged(Collections.singleton(req));
    verify(listener, VerificationModeFactory.times(2)).valueChanged(req);
  }
  
  @Test
  public void testSnapshotNoOverrides() throws InterruptedException {
    ValueRequirement req1 = getRequirement(1);
    ValueRequirement req2 = getRequirement(2);
    
    MockLiveDataSnapshotProvider p1 = new MockLiveDataSnapshotProvider("p1", true, 1);
    p1.put(req1, "value1");
    MockLiveDataSnapshotProvider p2 = new MockLiveDataSnapshotProvider("p2", true, 1);
    p2.put(req2, "value2");
    CombiningLiveDataSnapshotProvider provider = new CombiningLiveDataSnapshotProvider(Arrays.<LiveDataSnapshotProvider>asList(p1, p2));
    
    long snapshotTimestamp = provider.snapshot();
    assertEquals(1, p1.getAndResetSnapshotCount());
    assertEquals(1, p2.getAndResetSnapshotCount());
    
    assertEquals("value1", provider.querySnapshot(snapshotTimestamp, req1));
    assertEquals(1, p1.getAndResetQueryCount());
    assertEquals(0, p2.getAndResetQueryCount());
    
    assertEquals("value2", provider.querySnapshot(snapshotTimestamp, req2));
    assertEquals(1, p1.getAndResetQueryCount());
    assertEquals(1, p2.getAndResetQueryCount());
    
    ValueRequirement req3 = getRequirement(3);
    assertNull(provider.querySnapshot(snapshotTimestamp, req3));
    assertEquals(1, p1.getAndResetQueryCount());
    assertEquals(1, p2.getAndResetQueryCount());
    
    assertNull(provider.querySnapshot(1234, req1));
  }
  
  @Test
  public void testSnapshotWithOverrides() throws InterruptedException {
    ValueRequirement req1 = getRequirement(1);
    
    MockLiveDataSnapshotProvider p1 = new MockLiveDataSnapshotProvider("p1", true, 1);
    p1.put(req1, "value1");
    MockLiveDataSnapshotProvider p2 = new MockLiveDataSnapshotProvider("p2", true, 1);
    p2.put(req1, "value2");
    CombiningLiveDataSnapshotProvider provider = new CombiningLiveDataSnapshotProvider(Arrays.<LiveDataSnapshotProvider>asList(p1, p2));

    long snapshotTimestamp = provider.snapshot();
    assertEquals(1, p1.getAndResetSnapshotCount());
    assertEquals(1, p2.getAndResetSnapshotCount());
    
    // p1 should override the value in p2
    assertEquals("value1", provider.querySnapshot(snapshotTimestamp, req1));
    assertEquals(1, p1.getAndResetQueryCount());
    assertEquals(0, p2.getAndResetQueryCount());
  }

  private ValueRequirement getRequirement(int number) {
    return new ValueRequirement("Req-" + number, new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Target")));
  }
    
}