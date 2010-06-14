/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * An implementation of {@link LiveDataSnapshotProvider} which maintains an LKV
 * cache of externally provided values.
 * It is primarily useful for mock, testing, or demo scenarios.
 * 
 */
public class InMemoryLKVSnapshotProvider extends AbstractLiveDataSnapshotProvider implements LiveDataAvailabilityProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVSnapshotProvider.class);
  private final Map<ValueRequirement, ComputedValue> _lastKnownValues =
    new ConcurrentHashMap<ValueRequirement, ComputedValue>();
  private final Map<Long, Map<ValueRequirement, ComputedValue>> _snapshots =
    new ConcurrentHashMap<Long, Map<ValueRequirement, ComputedValue>>();

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    // Do nothing. All values are externally provided.
    s_logger.debug("Added subscription to {}", valueRequirement);
  }
  
  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    for (ValueRequirement requirement : valueRequirements) {
      addSubscription(user, requirement);      
    }
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    Map<ValueRequirement, ComputedValue> snapshotValues =
      _snapshots.get(snapshot);
    if (snapshotValues == null) {
      return null;
    }
    ComputedValue value = snapshotValues.get(requirement);
    if (value == null) {
      return null;
    }
    return value.getValue();
  }

  @Override
  public long snapshot() {
    long snapshotTime = System.currentTimeMillis();
    snapshot(snapshotTime);
    return snapshotTime;
  }
  
  /**
   * This method can be called directly to populate a historical
   * snapshot.
   * 
   * @param snapshotTime the time of the snapshot
   */
  public void snapshot(long snapshotTime) {
    Map<ValueRequirement, ComputedValue> snapshotValues =
      new HashMap<ValueRequirement, ComputedValue>(_lastKnownValues);
    _snapshots.put(snapshotTime, snapshotValues);
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    _snapshots.remove(snapshot);
  }
  
  public void addValue(ComputedValue value) {
    _lastKnownValues.put(value.getSpecification().getRequirementSpecification(), value);
    super.valueChanged(value.getSpecification().getRequirementSpecification());    
  }
  
  public Collection<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableCollection(_lastKnownValues.keySet());
  }
  
  public ComputedValue getCurrentValue(ValueRequirement valueRequirement) {
    return _lastKnownValues.get(valueRequirement);
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return _lastKnownValues.containsKey(requirement);
  }

}
