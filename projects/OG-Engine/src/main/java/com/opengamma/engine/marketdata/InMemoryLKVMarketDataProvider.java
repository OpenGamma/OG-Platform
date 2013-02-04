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

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * An implementation of {@link MarketDataProvider} which maintains an LKV cache of externally-provided values.
 */
public class InMemoryLKVMarketDataProvider extends AbstractMarketDataProvider implements MarketDataInjector, MarketDataAvailabilityProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataProvider.class);

  private final Map<ValueSpecification, Object> _lastKnownValues = new ConcurrentHashMap<ValueSpecification, Object>();
  // [PLAT-3044] Probably want a ComputationTargetResolver rather than a SecuritySource
  private final SecuritySource _securitySource;
  private final MarketDataPermissionProvider _permissionProvider;

  /**
   * Constructs an instance with no support for automatic resolution of Identifiers.
   */
  public InMemoryLKVMarketDataProvider() {
    this(null);
  }

  /**
   * Constructs an instance.
   * 
   * @param securitySource the security source for resolution of Identifiers, null to prevent this support
   */
  public InMemoryLKVMarketDataProvider(final SecuritySource securitySource) {
    _securitySource = securitySource;
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
    subscriptionSucceeded(valueSpecifications);
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
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return this;
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

  //-------------------------------------------------------------------------
  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    // [PLAT-3044] Do this properly - use the targetSpec
    return _lastKnownValues.containsKey(desiredValue) ? MarketDataUtils.createMarketDataValue(desiredValue, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addValue(final ValueSpecification specification, final Object value) {
    _lastKnownValues.put(specification, value);
    valueChanged(specification);
  }

  @Override
  public void addValue(final ValueRequirement requirement, final Object value) {
    addValue(resolveRequirement(requirement), value);
  }

  @Override
  public void removeValue(final ValueSpecification specification) {
    _lastKnownValues.remove(specification);
    valueChanged(specification);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    removeValue(resolveRequirement(valueRequirement));
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
    return new HashMap<ValueSpecification, Object>(_lastKnownValues);
  }

  protected ValueSpecification resolveRequirement(final ValueRequirement requirement) {
    // TODO [PLAT-3044] resolve the requirement to a specification
    throw new UnsupportedOperationException("[PLAT-3044] resolve " + requirement + " to a ValueSpecification");
  }

  /**
   * Gets the securitySource.
   * 
   * @return the securitySource
   */
  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
