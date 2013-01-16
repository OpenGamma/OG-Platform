/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Mock {@link MarketDataProvider}
 */
public class MockMarketDataProvider extends AbstractMarketDataProvider {
  
  private final String _name;
  private final boolean _subscriptionsSucceed;
  private final CountDownLatch _responseLatch;
  private final Map<ValueRequirement, ComputedValue> _values = new HashMap<ValueRequirement, ComputedValue>();
  private int _queryCount;
  private int _snapshotCount;
  
  public MockMarketDataProvider(String name, boolean subscriptionsSucceed, int subscriptionCount) {
    _name = name;
    _subscriptionsSucceed = subscriptionsSucceed;
    _responseLatch = new CountDownLatch(subscriptionCount);
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(final Set<ValueRequirement> valueRequirements) {
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
  public void unsubscribe(ValueRequirement valueRequirement) {
  }

  @Override
  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return null;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return null;
  }

  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    return false;
  }

  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    _snapshotCount++;
    return new MockMarketDataSnapshot(this);
  }
    
  //-------------------------------------------------------------------------
  public void awaitSubscriptionResponses() throws InterruptedException {
    _responseLatch.await();
  }
  
  public void put(ValueRequirement requirement, Object value) {
    _values.put(requirement, new ComputedValue(MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID), value));
  }
  
  /*package*/ void incrementQueryCount() {
    _queryCount++;
  }
  
  /*package*/ComputedValue getValue(ValueRequirement requirement) {
    return _values.get(requirement);
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
