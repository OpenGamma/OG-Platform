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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link MarketDataProvider} which maintains an LKV cache of externally-provided values.
 */
public class InMemoryLKVMarketDataProvider extends AbstractMarketDataProvider implements MarketDataInjector, MarketDataAvailabilityProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataProvider.class);
  
  private final Map<ValueRequirement, Object> _lastKnownValues = new ConcurrentHashMap<ValueRequirement, Object>();
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
  public InMemoryLKVMarketDataProvider(SecuritySource securitySource) {
    _securitySource = securitySource;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    subscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    unsubscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
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
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    return true;
  }

  @Override
  public InMemoryLKVMarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    return new InMemoryLKVMarketDataSnapshot(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailability getAvailability(ValueRequirement requirement) {
    return _lastKnownValues.containsKey(requirement) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addValue(ValueRequirement requirement, Object value) {
    _lastKnownValues.put(requirement, value);
    valueChanged(requirement);
  }
  
  @Override
  public void addValue(ExternalId identifier, String valueName, Object value) {
    ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    addValue(valueRequirement, value);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _lastKnownValues.remove(valueRequirement);
    valueChanged(valueRequirement);
  }
  
  @Override
  public void removeValue(ExternalId identifier, String valueName) {
    ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    removeValue(valueRequirement);
  }
  
  //-------------------------------------------------------------------------
  public Set<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableSet(_lastKnownValues.keySet());
  }

  public Object getCurrentValue(ValueRequirement valueRequirement) {
    return _lastKnownValues.get(valueRequirement);
  }
  
  //-------------------------------------------------------------------------
  /*package*/ Map<ValueRequirement, Object> doSnapshot() {
    return new HashMap<ValueRequirement, Object>(_lastKnownValues);
  }
  
  private ValueRequirement resolveRequirement(ExternalId identifier, String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    
    Security security = null;
    if (_securitySource != null) {
      // 1 - see if the identifier can be resolved to a security
      security = _securitySource.getSecurity(ExternalIdBundle.of(identifier));
      
      // 2 - see if the so-called Identifier is actually the UniqueId of a security
      // if (security == null) {
        // Can't do this as the UniqueId may be the wrong type for the master - does this case really matter?
        // security = _securitySource.getSecurity(uniqueIdentifier);
      // }
    }
    if (security != null) {
      return new ValueRequirement(valueName, ComputationTargetType.SECURITY, security.getUniqueId());
    } else {
      // 3 - treat the identifier as a UniqueId and assume it's a PRIMITIVE
      UniqueId uniqueIdentifier = UniqueId.of(identifier.getScheme().getName(), identifier.getValue());
      return new ValueRequirement(valueName, ComputationTargetType.PRIMITIVE, uniqueIdentifier);
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
