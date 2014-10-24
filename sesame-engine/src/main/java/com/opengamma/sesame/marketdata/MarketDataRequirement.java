/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

/**
 *
 */
public abstract class MarketDataRequirement {

  MarketDataRequirement() {
  }

  public abstract MarketDataId getMarketDataId();

  public abstract MarketDataTime getMarketDataTime();
}
