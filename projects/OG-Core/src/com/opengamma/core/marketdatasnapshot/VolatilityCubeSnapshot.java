package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

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
}
