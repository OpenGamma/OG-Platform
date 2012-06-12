/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A {@link ViewComputation} cache that can reschedule and condense concurrent put calls into single underlying calls.
 */
public interface DeferredViewComputationCache2 extends ViewComputationCache {

  void putSharedValue(ComputedValue value, DeferredStatistics statistics);

  void putPrivateValue(ComputedValue value, DeferredStatistics statistics);

  void putValue(ComputedValue value, CacheSelectHint filter, DeferredStatistics statistics);

  void putSharedValues(Collection<ComputedValue> values, DeferredStatistics statistics);

  void putPrivateValues(Collection<ComputedValue> values, DeferredStatistics statistics);

  void putValues(Collection<ComputedValue> values, CacheSelectHint filter, DeferredStatistics statistics);

  /**
   * Cause all put operations to complete. Do not call this concurrently with {@link #putValue} or {@link #putValues}. This may block until the operations have completed or return an asynchronous
   * handle to that additional work can be done.
   */
  void flush() throws AsynchronousExecution;
  
}
