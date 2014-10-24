/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

/**
 * Contains information about a piece of market data that is required for a calculation.
 */
public abstract class MarketDataRequirement {

  MarketDataRequirement() {
  }

  /**
   * @return the ID of the data
   */
  public abstract MarketDataId getMarketDataId();

  /**
   * @return the time for which the data is valid
   */
  public abstract MarketDataTime getMarketDataTime();
}
