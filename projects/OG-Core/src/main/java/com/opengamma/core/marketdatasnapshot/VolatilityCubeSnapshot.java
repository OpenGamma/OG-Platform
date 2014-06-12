/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.tuple.Triple;

/**
 * A snapshot of volatility cube data.
 * <p>
 * This class is mutable and not thread-safe.
 */
public interface VolatilityCubeSnapshot {

  /**
   * Gets the value snapshots.
   * 
   * @return the values
   */
  Map<Triple<Object, Object, Object>, ValueSnapshot> getValues();

}
