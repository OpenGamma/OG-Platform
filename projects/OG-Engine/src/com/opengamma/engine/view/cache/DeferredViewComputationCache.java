/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.calcnode.DeferredInvocationStatistics;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A {@link FilteredViewComputationCache} which allows rescheduling of the puts
 */
public abstract class DeferredViewComputationCache extends FilteredViewComputationCache {

  public DeferredViewComputationCache(ViewComputationCache cache, CacheSelectHint filter) {
    super(cache, filter);
  }

  public abstract void putValues(final Collection<ComputedValue> values, final DeferredInvocationStatistics statistics);

  /**
   * Cause all "write-behind" operations to complete. Do not call this concurrently with {@link #putValue} or {@link #putValues}. This may block until the operations have completed or return an
   * asynchronous handle to that additional work can be done.
   */
  public abstract void flush() throws AsynchronousExecution;

}
