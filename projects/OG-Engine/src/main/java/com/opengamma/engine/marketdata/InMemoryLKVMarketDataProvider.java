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

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link MarketDataProvider} which maintains an LKV cache of externally-provided values.
 */
public class InMemoryLKVMarketDataProvider extends AbstractMarketDataProvider implements MarketDataInjector, MarketDataAvailabilityProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataProvider.class);

  private final Map<ValueRequirement, ComputedValue> _lastKnownValues = new ConcurrentHashMap<ValueRequirement, ComputedValue>();
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
   * @param securitySource  the security source for resolution of Identifiers, null to prevent this support
   */
  public InMemoryLKVMarketDataProvider(final SecuritySource securitySource) {
    _securitySource = securitySource;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(final Set<ValueRequirement> valueRequirements) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }

  @Override
  public void unsubscribe(final ValueRequirement valueRequirement) {
    unsubscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(final Set<ValueRequirement> valueRequirements) {
    // No actual unsubscription to make
    s_logger.debug("Unsubscribed from {}", valueRequirements);
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
    // [PLAT-3044] Do this properly - use the target.toSpecification method
    return _lastKnownValues.containsKey(desiredValue) ? MarketDataUtils.createMarketDataValue(desiredValue, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addValue(final ValueRequirement requirement, final Object value) {
    _lastKnownValues.put(requirement, new ComputedValue(MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID), value));
    valueChanged(requirement);
  }

  @Override
  public void addValue(final ExternalId identifier, final String valueName, final Object value) {
    final ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    addValue(valueRequirement, value);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _lastKnownValues.remove(valueRequirement);
    valueChanged(valueRequirement);
  }

  @Override
  public void removeValue(final ExternalId identifier, final String valueName) {
    final ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    removeValue(valueRequirement);
  }

  //-------------------------------------------------------------------------
  public Set<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableSet(_lastKnownValues.keySet());
  }

  public ComputedValue getCurrentValue(final ValueRequirement valueRequirement) {
    return _lastKnownValues.get(valueRequirement);
  }

  //-------------------------------------------------------------------------
  /*package*/Map<ValueRequirement, ComputedValue> doSnapshot() {
    return new HashMap<ValueRequirement, ComputedValue>(_lastKnownValues);
  }

  private ValueRequirement resolveRequirement(final ExternalId identifier, final String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    Security security = null;
    if (_securitySource != null) {
      // 1 - see if the identifier can be resolved to a security
      security = _securitySource.getSingle(ExternalIdBundle.of(identifier));

      // 2 - see if the so-called Identifier is actually the UniqueId of a security
      // if (security == null) {
      // Can't do this as the UniqueId may be the wrong type for the master - does this case really matter?
      // security = _securitySource.getSecurity(uniqueIdentifier);
      // }
    }
    if (security != null) {
      return new ValueRequirement(valueName, ComputationTargetType.SECURITY, security.getUniqueId());
    } else {
      // 3 - assume it's a PRIMITIVE
      return new ValueRequirement(valueName, ComputationTargetType.PRIMITIVE, identifier);
    }
  }

  /**
   * Gets the securitySource.
   * @return the securitySource
   */
  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
