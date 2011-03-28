package com.opengamma.core.marketdatasnapshot;

import javax.time.Instant;


/**
 * Represent a snapshot of a yield curve in a {@link StructuredMarketDataSnapshot}
 * @see StructuredMarketDataSnapshot
 */
public interface YieldCurveSnapshot {
  
  /**
   * @return The instant at which this YieldCurve was evaluated in order to generate the snapshot keys
   */
  Instant getValuationTime();
  
  /**
   * @return The values which should be applied when building this curve
   */
  UnstructuredMarketDataSnapshot getValues();
}
