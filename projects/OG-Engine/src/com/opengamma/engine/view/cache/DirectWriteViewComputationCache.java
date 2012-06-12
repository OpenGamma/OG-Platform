/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.calcnode.DeferredInvocationStatistics;

/**
 *  A {@link DeferredViewComputationCache} which doesn't reschedule the puts
 */
public class DirectWriteViewComputationCache extends DeferredViewComputationCache {

  public DirectWriteViewComputationCache(ViewComputationCache cache, CacheSelectHint filter) {
    super(cache, filter);
  }

  @Override
  public void putValues(Collection<ComputedValue> values, DeferredInvocationStatistics statistics) {
    super.putValues(values);
    for (ComputedValue computedValue : values) {
      statistics.addDataOutputBytes(estimateValueSize(computedValue));
    }
  }

  @Override
  public void flush() {
    // No-op - all writes already completed by definition
  }
}
