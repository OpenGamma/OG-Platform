/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 * An implementation of {@link LiveDataSnapshotProvider} which maintains an LKV
 * cache of externally provided values.
 * It is primarily useful for mock, testing, or demo scenarios.
 *
 * @author kirk
 */
public class InMemoryLKVSnapshotProvider implements LiveDataSnapshotProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVSnapshotProvider.class);
  private final Map<ValueRequirement, NewComputedValue> _lastKnownValues =
    new HashMap<ValueRequirement, NewComputedValue>();
  private final Map<Long, Map<ValueRequirement, NewComputedValue>> _snapshots =
    new HashMap<Long, Map<ValueRequirement, NewComputedValue>>();

  @Override
  public void addSubscription(ValueRequirement valueRequirement) {
    // Do nothing. All values are externally provided.
    s_logger.debug("Added subscription to {}", valueRequirement);
  }

  @Override
  public synchronized Object querySnapshot(long snapshot, ValueRequirement requirement) {
    Map<ValueRequirement, NewComputedValue> snapshotValues =
      _snapshots.get(snapshot);
    if(snapshotValues == null) {
      return null;
    }
    NewComputedValue value = snapshotValues.get(requirement);
    if(value == null) {
      return null;
    }
    return value.getValue();
  }

  @Override
  public synchronized long snapshot() {
    long snapshotTime = System.currentTimeMillis();
    Map<ValueRequirement, NewComputedValue> snapshotValues =
      new HashMap<ValueRequirement, NewComputedValue>(_lastKnownValues);
    _snapshots.put(snapshotTime, snapshotValues);
    return snapshotTime;
  }

  @Override
  public synchronized void releaseSnapshot(long snapshot) {
    _snapshots.remove(snapshot);
  }
  
  public synchronized void addValue(NewComputedValue value) {
    _lastKnownValues.put(value.getSpecification().getRequirementSpecification(), value);
  }
  
  public synchronized Collection<ValueRequirement> getAllValueKeys() {
    return new ArrayList<ValueRequirement>(_lastKnownValues.keySet());
  }
  
  public synchronized NewComputedValue getCurrentValue(ValueRequirement valueRequirement) {
    return _lastKnownValues.get(valueRequirement);
  }

}
