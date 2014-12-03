/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

/**
 * Key for identifying market data in {@link ScenarioMarketDataEnvironment} and requesting it from {@link MarketDataBundle}.
 * <p>
 * Market data is any data provided to the system from an external source. It can be simple values (for example
 * FX rates) or high level objects (for example a calibrated curve).
 */
public interface MarketDataId<T> {

  /**
   * @return the type of the market data identified by this ID
   */
  public Class<T> getMarketDataType();
}
