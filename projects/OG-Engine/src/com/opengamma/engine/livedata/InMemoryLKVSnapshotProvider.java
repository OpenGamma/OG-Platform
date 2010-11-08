/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;

/**
 * An implementation of {@link LiveDataSnapshotProvider} which maintains an LKV cache of externally provided values.
 */
public class InMemoryLKVSnapshotProvider extends AbstractLiveDataSnapshotProvider implements
    LiveDataInjector, LiveDataAvailabilityProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVSnapshotProvider.class);
  private final Map<ValueRequirement, Object> _lastKnownValues = new ConcurrentHashMap<ValueRequirement, Object>();
  private final Map<Long, Map<ValueRequirement, Object>> _snapshots = new ConcurrentHashMap<Long, Map<ValueRequirement, Object>>();

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

  @Override
  public void addValue(ValueRequirement requirement, Object value) {
    _lastKnownValues.put(requirement, value);
    valueChanged(requirement);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _lastKnownValues.remove(valueRequirement);
    valueChanged(valueRequirement);
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

}
