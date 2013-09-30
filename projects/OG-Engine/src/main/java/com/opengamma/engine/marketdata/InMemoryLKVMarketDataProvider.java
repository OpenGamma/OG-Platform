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
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * An implementation of {@link MarketDataProvider} which maintains an LKV cache of externally-provided values.
 */
public class InMemoryLKVMarketDataProvider extends AbstractMarketDataProvider implements MarketDataInjector {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataProvider.class);

  private final Map<ValueSpecification, Object> _lastKnownValues = new ConcurrentHashMap<ValueSpecification, Object>();
  private final FixedMarketDataAvailabilityProvider _availability = new FixedMarketDataAvailabilityProvider();
  private final MarketDataPermissionProvider _permissionProvider;

  /**
   * Constructs an instance.
   */
  public InMemoryLKVMarketDataProvider() {
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueSpecifications);
    subscriptionsSucceeded(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    // No actual unsubscription to make
    s_logger.debug("Unsubscribed from {}", valueSpecifications);
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return _availability;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return true;
  }

  @Override
  public InMemoryLKVMarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return new InMemoryLKVMarketDataSnapshot(this);
  }

  @Override
  public void addValue(final ValueSpecification specification, final Object value) {
    if (value != null) {
      _lastKnownValues.put(specification, value);
    }
    _availability.addAvailableData(specification);
    valueChanged(specification);
  }

  @Override
  public void addValue(final ValueRequirement requirement, final Object value) {
    final ValueSpecification resolved = _availability.resolveRequirement(requirement);
    addValue(resolved, value);
  }

  @Override
  public void removeValue(final ValueSpecification specification) {
    _availability.removeAvailableData(specification);
    _lastKnownValues.remove(specification);
    valueChanged(specification);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    final ValueSpecification resolved = _availability.resolveRequirement(valueRequirement);
    removeValue(resolved);
  }

  //-------------------------------------------------------------------------
  public Set<ValueSpecification> getAllValueKeys() {
    return Collections.unmodifiableSet(_lastKnownValues.keySet());
  }

  public Object getCurrentValue(final ValueSpecification specification) {
    return _lastKnownValues.get(specification);
  }

  //-------------------------------------------------------------------------

  /*package*/Map<ValueSpecification, Object> doSnapshot() {
    return new HashMap<>(_lastKnownValues);
  }

}
