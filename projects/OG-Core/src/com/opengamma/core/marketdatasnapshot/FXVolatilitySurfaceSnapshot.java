/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Represent a snapshot of an FX volatility surface along with its spot rate in a MarketDataSnapshot
 * @see MarketDataSnapshot
 */
public class FXVolatilitySurfaceSnapshot {

  private final MarketDataSnapshot _spotRate;
  private final Map<Pair<Tenor, Tenor>, ValueSnapshot> _values;

  public FXVolatilitySurfaceSnapshot(MarketDataSnapshot spotRate, Map<Pair<Tenor, Tenor>, ValueSnapshot> values) {
    super();
    _spotRate = spotRate;
    _values = values;
  }

  public MarketDataSnapshot getSpotRate() {
    return _spotRate;
  }

  public Map<Pair<Tenor, Tenor>, ValueSnapshot> getValues() {
    return _values;
  }

}
