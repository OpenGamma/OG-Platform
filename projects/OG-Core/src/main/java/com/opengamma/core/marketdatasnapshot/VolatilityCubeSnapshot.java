/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * A snapshot of volatility cube data.
 * <p>
 * This class is mutable and not thread-safe.
 */
public interface VolatilityCubeSnapshot {

  /**
   * Gets the value snapshots by point.
   * 
   * @return the values
   */
  Map<VolatilityPoint, ValueSnapshot> getValues();

  /**
   * Gets the unstructured market data snapshot.
   * <p>
   * This contains the other values that should be applied when building the cube, such as the spot rate.
   * 
   * @return the values which should be applied when building this curve
   */
  UnstructuredMarketDataSnapshot getOtherValues();

  /**
   * Gets the strikes.
   * 
   * @return the strikes
   */
  Map<Pair<Tenor, Tenor>, ValueSnapshot> getStrikes();

}
