/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;

/**
 *
 */
public class ChainedMarketDataSubscriberTest {

  /**
   * Tests that a chain with one link works. i.e. that a single subscriber correctly subscribes and unsubscribes
   * from the underlying provider and receives notifications.
   */
  @Test
  public void singleProvider() {
    ProviderWithSpec providerWithSpec = new ProviderWithSpec(new TestProvider(), new LiveMarketDataSpecification("spec"));
    TestListener listener = new TestListener();
    List<ProviderWithSpec> providerList = Collections.singletonList(providerWithSpec);
    ChainedMarketDataSubscriber subscriber = ChainedMarketDataSubscriber.createChain(providerList, listener);

  }
}

class TestListener implements MarketDataListener {

  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
    // TODO implement subscriptionSucceeded()
    throw new UnsupportedOperationException("subscriptionSucceeded not implemented");
  }

  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
    // TODO implement subscriptionFailed()
    throw new UnsupportedOperationException("subscriptionFailed not implemented");
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {
    // TODO implement subscriptionStopped()
    throw new UnsupportedOperationException("subscriptionStopped not implemented");
  }

  @Override
  public void valuesChanged(Collection<ValueRequirement> requirements) {
    // TODO implement valuesChanged()
    throw new UnsupportedOperationException("valuesChanged not implemented");
  }
}

class TestProvider implements MarketDataProvider {

  private MarketDataListener _listener;

  public MarketDataListener getListener() {
    assertNotNull(_listener);
    return _listener;
  }

  @Override
  public void addListener(MarketDataListener listener) {
    assertNotNull(listener);
    _listener = listener;
  }

  @Override
  public void removeListener(MarketDataListener listener) {
    // TODO implement removeListener()
    throw new UnsupportedOperationException("removeListener not implemented");
  }

  @Override
  public void subscribe(ValueRequirement valueRequirement) {
    // TODO implement subscribe()
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  @Override
  public void subscribe(Set<ValueRequirement> valueRequirements) {
    // TODO implement subscribe()
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  @Override
  public void unsubscribe(ValueRequirement valueRequirement) {
    // TODO implement unsubscribe()
    throw new UnsupportedOperationException("unsubscribe not implemented");
  }

  @Override
  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    // TODO implement unsubscribe()
    throw new UnsupportedOperationException("unsubscribe not implemented");
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    // TODO implement getAvailabilityProvider()
    throw new UnsupportedOperationException("getAvailabilityProvider not implemented");
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    // TODO implement getPermissionProvider()
    throw new UnsupportedOperationException("getPermissionProvider not implemented");
  }

  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    // TODO implement isCompatible()
    throw new UnsupportedOperationException("isCompatible not implemented");
  }

  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    // TODO implement snapshot()
    throw new UnsupportedOperationException("snapshot not implemented");
  }

  @Override
  public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
    // TODO implement getRealTimeDuration()
    throw new UnsupportedOperationException("getRealTimeDuration not implemented");
  }
}