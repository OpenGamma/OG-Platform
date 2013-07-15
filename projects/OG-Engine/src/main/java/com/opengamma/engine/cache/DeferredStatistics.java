/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import com.opengamma.engine.value.ComputedValue;

/**
 * Callback interface for receiving size estimate notifications after a deferred write operation.
 */
public interface DeferredStatistics {

  /**
   * Reports the size of a value as estimated by a cache after it has processed it.
   * 
   * @param value the value processed
   * @param bytes the estimated size or null if unknown
   */
  void reportEstimatedSize(ComputedValue value, Integer bytes);

}
