/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A {@link ViewComputation} cache that can reschedule and condense concurrent put calls into single underlying calls.
 */
public interface DeferredViewComputationCache extends ViewComputationCache {

  void putSharedValue(ComputedValue value, DeferredStatistics statistics);

  void putPrivateValue(ComputedValue value, DeferredStatistics statistics);

  void putValue(ComputedValue value, CacheSelectHint filter, DeferredStatistics statistics);

  void putSharedValues(Collection<? extends ComputedValue> values, DeferredStatistics statistics);

  void putPrivateValues(Collection<? extends ComputedValue> values, DeferredStatistics statistics);

  void putValues(Collection<? extends ComputedValue> values, CacheSelectHint filter, DeferredStatistics statistics);

  /**
   * Cause all put operations issued by this thread to complete. This may block until the operations have completed or return an asynchronous handle to that additional work can be done.
   */
  void flush() throws AsynchronousExecution;

}
