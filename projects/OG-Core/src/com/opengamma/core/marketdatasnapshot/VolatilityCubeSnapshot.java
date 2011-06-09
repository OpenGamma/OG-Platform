package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

/**
 * 
 */
public interface VolatilityCubeSnapshot {

  Map<VolatilityPoint, ValueSnapshot> getValues();
}
