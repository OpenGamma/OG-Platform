/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.spec;

import java.io.Serializable;

import com.opengamma.engine.marketdata.historical.HistoricalShockMarketDataSnapshot;
import com.opengamma.util.ArgumentChecker;

/**
 * Specification for market data derived from 3 underlying providers. The values are derived by finding the
 * difference between values in the first two providers and applying it to the value from the third provider.
 * The change applied to the base value can be the proportional or absolute difference between the two other values.
 */
public class HistoricalShockMarketDataSpecification extends MarketDataSpecification implements Serializable {

  private final HistoricalShockMarketDataSnapshot.ShockType _shockType;
  private final MarketDataSpecification _historicalSpec1;
  private final MarketDataSpecification _historicalSpec2;
  private final MarketDataSpecification _baseSpec;

  public HistoricalShockMarketDataSpecification(HistoricalShockMarketDataSnapshot.ShockType shockType,
                                                MarketDataSpecification historicalSpec1,
                                                MarketDataSpecification historicalSpec2,
                                                MarketDataSpecification baseSpec) {
    ArgumentChecker.notNull(historicalSpec1, "historicalSpec1");
    ArgumentChecker.notNull(historicalSpec2, "historicalSpec2");
    ArgumentChecker.notNull(baseSpec, "baseSpec");
    _shockType = shockType;
    _historicalSpec1 = historicalSpec1;
    _historicalSpec2 = historicalSpec2;
    _baseSpec = baseSpec;
  }

  /**
   * @return Specification of the first source of underlying data
   */
  public MarketDataSpecification getHistoricalSpecification1() {
    return _historicalSpec1;
  }

  /**
   * @return Specification of the second source of underlying data
   */
  public MarketDataSpecification getHistoricalSpecification2() {
    return _historicalSpec2;
  }

  /**
   * @return Specification of the source of data whose data is the basis for the shocked value.
   */
  public MarketDataSpecification getBaseSpecification() {
    return _baseSpec;
  }

  /**
   * @return Whether the absolute or proportional difference between the first two sources is applied to the third.
   */
  public HistoricalShockMarketDataSnapshot.ShockType getShockType() {
    return _shockType;
  }
}
