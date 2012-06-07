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
 * A {@link FilteredViewComputationCache} which allows rescheduling of the puts
 */
public abstract class DeferredViewComputationCache extends FilteredViewComputationCache {

  public DeferredViewComputationCache(ViewComputationCache cache, CacheSelectHint filter) {
    super(cache, filter);
  }

  public abstract void putValues(final Collection<ComputedValue> values, final DeferredInvocationStatistics statistics);
  
  /**
   * Block until all "write-behind" operations have completed. Do not call this concurrently with
   * {@link #putValue} or {@link #putValues}.
   */
  public abstract void waitForPendingWrites();
}
