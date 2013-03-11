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
import com.opengamma.engine.value.ValueSpecification;

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
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    // No-op
  }

  /**
   * Implementation based on the {@link #query(ValueSpecification)} method. {@inheritDoc}
   */
  @Override
  public Map<ValueSpecification, Object> query(final Set<ValueSpecification> specifications) {
    final Map<ValueSpecification, Object> results = Maps.newHashMapWithExpectedSize(specifications.size());
    for (final ValueSpecification specification : specifications) {
      final Object value = query(specification);
      if (value != null) {
        results.put(specification, value);
      }
    }
    return results;
  }
  
  protected void assertInitialized() {
    if (!isInitialized()) {
      throw new IllegalStateException("Market data snapshot is not initialized");
    }
  }

}
