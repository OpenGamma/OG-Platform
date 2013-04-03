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
import com.opengamma.engine.value.ValueSpecification;

/**
 * Mock {@link MarketDataProvider}
 */
public class MockMarketDataProvider extends AbstractMarketDataProvider {

  private final String _name;
  private final boolean _subscriptionsSucceed;
  private final CountDownLatch _responseLatch;
  private final Map<ValueSpecification, Object> _values = new HashMap<ValueSpecification, Object>();
  private int _queryCount;
  private int _snapshotCount;

  public MockMarketDataProvider(final String name, final boolean subscriptionsSucceed, final int subscriptionCount) {
    _name = name;
    _subscriptionsSucceed = subscriptionsSucceed;
    _responseLatch = new CountDownLatch(subscriptionCount);
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    final Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        if (_subscriptionsSucceed) {
          subscriptionsSucceeded(valueSpecifications);
        } else {
          subscriptionFailed(valueSpecifications, _name);
        }
        _responseLatch.countDown();
      }
    });
    t.start();
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return null;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return null;
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return false;
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    _snapshotCount++;
    return new MockMarketDataSnapshot(this);
  }

  //-------------------------------------------------------------------------
  public void awaitSubscriptionResponses() throws InterruptedException {
    _responseLatch.await();
  }

  public void put(final ValueSpecification specification, final Object value) {
    _values.put(specification, value);
  }

  /*package*/void incrementQueryCount() {
    _queryCount++;
  }

  /*package*/Object getValue(final ValueSpecification specification) {
    return _values.get(specification);
  }

  public int getAndResetQueryCount() {
    final int count = _queryCount;
    _queryCount = 0;
    return count;
  }

  public int getAndResetSnapshotCount() {
    final int count = _snapshotCount;
    _snapshotCount = 0;
    return count;
  }

}
