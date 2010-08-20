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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

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
  
  private class MockLiveDataSnapshotProvider extends AbstractLiveDataSnapshotProvider {
    
    private final String _name;
    private final boolean _subscriptionsSucceed;
    private final List<ValueRequirement> _subscribed = new ArrayList<ValueRequirement>();
    private final CountDownLatch _responseLatch;
    private final Map<ValueRequirement, Object> _values = new HashMap<ValueRequirement, Object>();
    private int _queryCount = 0;
    private int _snapshotCount = 0;
    
    public MockLiveDataSnapshotProvider(String name, boolean subscriptionsSucceed, int subscriptionCount) {
      _name = name;
      _subscriptionsSucceed = subscriptionsSucceed;
      _responseLatch = new CountDownLatch(subscriptionCount);
    }

    @Override
    public void addSubscription(UserPrincipal user, final ValueRequirement valueRequirement) {
      addSubscription(user, Collections.singleton(valueRequirement));
    }

    @Override
    public void addSubscription(UserPrincipal user, final Set<ValueRequirement> valueRequirements) {
      _subscribed.addAll(valueRequirements);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          if (_subscriptionsSucceed) {
            subscriptionSucceeded(valueRequirements);
          } else {
            subscriptionFailed(valueRequirements, _name);
          }
          _responseLatch.countDown();
        }
      });
      t.start();
    }

    @Override
    public Object querySnapshot(long snapshot, ValueRequirement requirement) {
      _queryCount++;
      return _values.get(requirement);
    }

    @Override
    public void releaseSnapshot(long snapshot) {
    }

    @Override
    public long snapshot() {
      _snapshotCount++;
      return System.currentTimeMillis();
    }
    
    public void awaitSubscriptionResponses() throws InterruptedException {
      _responseLatch.await();
    }
    
    public void put(ValueRequirement requirement, Object value) {
      _values.put(requirement, value);
    }
    
    public int getAndResetQueryCount() {
      int count = _queryCount;
      _queryCount = 0;
      return count;
    }
    
    public int getAndResetSnapshotCount() {
      int count = _snapshotCount;
      _snapshotCount = 0;
      return count;
    }
    
  }
    
}