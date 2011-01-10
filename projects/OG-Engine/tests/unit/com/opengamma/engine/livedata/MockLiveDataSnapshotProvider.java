/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;

/**
 * 
 */
public class MockLiveDataSnapshotProvider extends AbstractLiveDataSnapshotProvider {
  
  private final String _name;
  private final boolean _subscriptionsSucceed;
  private final List<ValueRequirement> _subscribed = new ArrayList<ValueRequirement>();
  private final CountDownLatch _responseLatch;
  private final Map<ValueRequirement, Object> _values = new HashMap<ValueRequirement, Object>();
  private int _queryCount;
  private int _snapshotCount;
  
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
  
  @Override
  public long snapshot(long snapshot) {
    _snapshotCount++;
    return snapshot;
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
