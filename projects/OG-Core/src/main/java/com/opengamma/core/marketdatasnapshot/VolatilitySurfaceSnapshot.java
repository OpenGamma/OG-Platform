/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.tuple.Pair;

/**
 * A snapshot of volatility surface data.
 * <p>
 * This class is mutable and not thread-safe.
 */
public interface VolatilitySurfaceSnapshot {

  /**
   * Gets the value snapshots.
   * 
   * @return the values
   */
  Map<Pair<Object, Object>, ValueSnapshot> getValues();

}
