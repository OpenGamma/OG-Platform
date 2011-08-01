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
 * 
 */
public interface VolatilityCubeSnapshot {

  Map<VolatilityPoint, ValueSnapshot> getValues();
  
  /**
   * Gets the unstructured market data snapshot.
   * <p>
   * This contains the otehr values that should be applied when building the cube (e.g. spot rate)
   * 
   * @return the values which should be applied when building this curve
   */
  UnstructuredMarketDataSnapshot getOtherValues();
  
  Map<Pair<Tenor, Tenor>, ValueSnapshot> getStrikes();
  
  
}
