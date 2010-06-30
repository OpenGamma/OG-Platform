/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Temporary demo code. Can override any arbitrary live data value to sit on top of a proper
 * live data provider.
 */
public class OverrideLiveData implements LiveDataAvailabilityProvider, LiveDataSnapshotProvider,
    LiveDataSnapshotListener {

  private static final Logger s_logger = LoggerFactory.getLogger(OverrideLiveData.class);

  private final Set<LiveDataSnapshotListener> _listeners = new CopyOnWriteArraySet<LiveDataSnapshotListener>();
  private final LiveDataAvailabilityProvider _underlyingAvailability;
  private final LiveDataSnapshotProvider _underlyingSnapshot;
  private final Map<ValueRequirement, Object> _overrides = new ConcurrentHashMap<ValueRequirement, Object>();
  private final Map<Long, Map<ValueRequirement, Object>> _snapshot = new ConcurrentHashMap<Long, Map<ValueRequirement, Object>>();

  public OverrideLiveData(final LiveDataAvailabilityProvider underlyingAvailability,
      final LiveDataSnapshotProvider underlyingSnapshot) {
    ArgumentChecker.notNull(underlyingAvailability, "underlyingAvailability");
    ArgumentChecker.notNull(underlyingSnapshot, "underlyingSnapshot");
    _underlyingAvailability = underlyingAvailability;
    _underlyingSnapshot = underlyingSnapshot;
    _underlyingSnapshot.addListener(this);
  }

  public void putValue(final ValueRequirement valueRequirement, final Object value) {
    _overrides.put(valueRequirement, value);
    valueChangedImpl(valueRequirement);
  }

  public void removeValue(final ValueRequirement valueRequirement) {
    _overrides.remove(valueRequirement);
    valueChangedImpl(valueRequirement);
  }

  public Object getValue(final ValueRequirement valueRequirement) {
    return _overrides.get(valueRequirement);
  }

  public Map<ValueRequirement, Object> getAllValues() {
    return new HashMap<ValueRequirement, Object>(_overrides);
  }

  public void removeAllValues() {
    final Set<ValueRequirement> values = new HashSet<ValueRequirement>(_overrides.keySet());
    _overrides.clear();
    for (ValueRequirement value : values) {
      valueChangedImpl(value);
    }
  }

  protected void valueChangedImpl(final ValueRequirement valueRequirement) {
    if (valueRequirement.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
      s_logger.debug("Primitive value changed {}", valueRequirement);
    }
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.valueChanged(valueRequirement);
    }
  }

  // LiveDataAvailabilityProvider

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return _overrides.containsKey(requirement) || _underlyingAvailability.isAvailable(requirement);
  }

  // LiveDataSnapshotProvider

  @Override
  public void addListener(LiveDataSnapshotListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    _underlyingSnapshot.addSubscription(user, valueRequirement);
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    _underlyingSnapshot.addSubscription(user, valueRequirements);
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    final Map<ValueRequirement, Object> data = _snapshot.get(snapshot);
    if (data == null) {
      return null;
    }
    Object value = data.get(requirement);
    if (value != null) {
      return value;
    }
    value = _underlyingSnapshot.querySnapshot(snapshot, requirement);
    return value;
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    _snapshot.remove(snapshot);
    _underlyingSnapshot.releaseSnapshot(snapshot);
  }

  @Override
  public long snapshot() {
    final Map<ValueRequirement, Object> overrides = new HashMap<ValueRequirement, Object>(_overrides);
    final long timestamp = _underlyingSnapshot.snapshot();
    _snapshot.put(timestamp, overrides);
    return timestamp;
  }

  // LiveDataSnapshotListener

  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionFailed(requirement, msg);
    }
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionStopped(requirement);
    }
  }

  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionSucceeded(requirement);
    }
  }

  @Override
  public void valueChanged(ValueRequirement requirement) {
    if (!_overrides.containsKey(requirement)) {
      valueChangedImpl(requirement);
    }
  }

}
