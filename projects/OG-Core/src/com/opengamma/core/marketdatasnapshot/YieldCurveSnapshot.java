package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.time.Tenor;

/**
 * Represent a snapshot of a yield curve in a MarketDataSnapshot
 * @see MarketDataSnapshot
 */
public class YieldCurveSnapshot {
  private final Map<Tenor, ValueSnapshot> _values;

  public YieldCurveSnapshot(Map<Tenor, ValueSnapshot> values) {
    super();
    _values = values;
  }

  /**
   * Gets the values field.
   * @return the values
   */
  public Map<Tenor, ValueSnapshot> getValues() {
    return _values;
  }
  
  
}
