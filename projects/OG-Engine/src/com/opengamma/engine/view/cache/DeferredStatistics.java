/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.value.ComputedValue;

/**
 * Callback interface for receiving size estimate notifications after a deferred write operation.
 */
public interface DeferredStatistics {

  void reportEstimatedSize(ComputedValue value, Integer bytes);

}
