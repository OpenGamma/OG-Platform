/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link LiveDataSnapshotProvider} which maintains an LKV cache of externally provided values.
 */
public class InMemoryLKVSnapshotProvider extends AbstractLiveDataSnapshotProvider implements LiveDataInjector, LiveDataAvailabilityProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVSnapshotProvider.class);
  
  private final Map<ValueRequirement, Object> _lastKnownValues = new ConcurrentHashMap<ValueRequirement, Object>();
  private final Map<Long, Map<ValueRequirement, Object>> _snapshots = new ConcurrentHashMap<Long, Map<ValueRequirement, Object>>();
  private final SecuritySource _securitySource;

  /**
   * Constructs an instance with no support for automatic resolution of Identifiers.
   */
  public InMemoryLKVSnapshotProvider() {
    this(null);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param securitySource  the security source for resolution of Identifiers, or {@code null} to prevent this support
   */
  public InMemoryLKVSnapshotProvider(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    addSubscription(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    Map<ValueRequirement, Object> snapshotValues = _snapshots.get(snapshot);
    if (snapshotValues == null) {
      return null;
    }
    Object value = snapshotValues.get(requirement);
    return value;
  }

  @Override
  public long snapshot() {
    long snapshotTime = System.currentTimeMillis();
    snapshot(snapshotTime);
    return snapshotTime;
  }

  @Override
  public long snapshot(long snapshotTime) {
    Map<ValueRequirement, Object> snapshotValues = new HashMap<ValueRequirement, Object>(_lastKnownValues);
    _snapshots.put(snapshotTime, snapshotValues);
    return snapshotTime;
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    _snapshots.remove(snapshot);
  }
 
  public Set<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableSet(_lastKnownValues.keySet());
  }

  public Object getCurrentValue(ValueRequirement valueRequirement) {
    return _lastKnownValues.get(valueRequirement);
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return _lastKnownValues.containsKey(requirement);
  }

  //-------------------------------------------------------------------------
  @Override
  public void addValue(ValueRequirement requirement, Object value) {
    _lastKnownValues.put(requirement, value);
    valueChanged(requirement);
  }
  
  @Override
  public void addValue(Identifier identifier, String valueName, Object value) {
    ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    addValue(valueRequirement, value);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _lastKnownValues.remove(valueRequirement);
    valueChanged(valueRequirement);
  }
  
  @Override
  public void removeValue(Identifier identifier, String valueName) {
    ValueRequirement valueRequirement = resolveRequirement(identifier, valueName);
    removeValue(valueRequirement);
  }
  
  //-------------------------------------------------------------------------
  private ValueRequirement resolveRequirement(Identifier identifier, String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    
    Security security = null;
    UniqueIdentifier uniqueIdentifier = identifier.toUniqueIdentifier();
    if (_securitySource != null) {
      // 1 - see if the identifier can be resolved to a security
      security = _securitySource.getSecurity(IdentifierBundle.of(identifier));
      
      // 2 - see if the so-called Identifier is actually the UniqueIdentifier of a security
      if (security == null) {
        security = _securitySource.getSecurity(uniqueIdentifier);
      }
    }
    
    if (security != null) {
      return new ValueRequirement(valueName, ComputationTargetType.SECURITY, security.getUniqueId());
    } else {
      // 3 - treat the identifier as a UniqueIdentifier and assume it's a PRIMITIVE
      return new ValueRequirement(valueName, ComputationTargetType.PRIMITIVE, uniqueIdentifier);
    }

  }

}
