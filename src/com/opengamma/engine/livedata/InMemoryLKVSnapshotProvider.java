/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * An implementation of {@link LiveDataSnapshotProvider} which maintains an LKV
 * cache of externally provided values.
 * It is primarily useful for mock, testing, or demo scenarios.
 *
 * @author kirk
 */
public class InMemoryLKVSnapshotProvider implements LiveDataSnapshotProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVSnapshotProvider.class);
  private final Map<AnalyticValueDefinition<?>, ComputedValue<?>> _lastKnownValues =
    new HashMap<AnalyticValueDefinition<?>, ComputedValue<?>>();
  private final Map<Long, Map<AnalyticValueDefinition<?>, ComputedValue<?>>> _snapshots =
    new HashMap<Long, Map<AnalyticValueDefinition<?>, ComputedValue<?>>>();

  @Override
  public void addSubscription(AnalyticValueDefinition<?> definition) {
    // Do nothing. All values are externally provided.
    s_logger.debug("Added subscription to {}", definition);
  }

  @Override
  public synchronized Object querySnapshot(long snapshot, AnalyticValueDefinition<?> definition) {
    Map<AnalyticValueDefinition<?>, ComputedValue<?>> snapshotValues =
      _snapshots.get(snapshot);
    if(snapshotValues == null) {
      return null;
    }
    ComputedValue<?> value = snapshotValues.get(definition);
    if(value == null) {
      return null;
    }
    return value.getValue();
  }

  @Override
  public synchronized long snapshot() {
    long snapshotTime = System.currentTimeMillis();
    Map<AnalyticValueDefinition<?>, ComputedValue<?>> snapshotValues =
      new HashMap<AnalyticValueDefinition<?>, ComputedValue<?>>(_lastKnownValues);
    _snapshots.put(snapshotTime, snapshotValues);
    return snapshotTime;
  }

  @Override
  public synchronized void releaseSnapshot(long snapshot) {
    _snapshots.remove(snapshot);
  }
  
  public synchronized void addValue(ComputedValue<?> value) {
    _lastKnownValues.put(value.getDefinition(), value);
  }

}
