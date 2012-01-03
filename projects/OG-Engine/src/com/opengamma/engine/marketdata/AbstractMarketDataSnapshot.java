/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Base implementation of {@link MarketDataSnapshot}.
 */
public abstract class AbstractMarketDataSnapshot implements MarketDataSnapshot {

  /**
   * No-op implementation.
   */
  @Override
  public void init() {
    // No-op
  }

  /**
   * No-op implementation.
   * 
   * {@inheritDoc}
   */
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    // No-op
  }

  /**
   * Implementation based on the {@link #query(ValueRequirement)} method.
   * 
   * {@inheritDoc}
   */
  @Override
  public Map<ValueRequirement, Object> query(final Set<ValueRequirement> requirements) {
    final Map<ValueRequirement, Object> results = Maps.newHashMapWithExpectedSize(requirements.size());
    for (ValueRequirement requirement : requirements) {
      final Object value = query(requirement);
      if (value != null) {
        results.put(requirement, value);
      }
    }
    return results;
  }

}
