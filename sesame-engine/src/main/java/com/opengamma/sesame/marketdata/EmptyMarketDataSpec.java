/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Place holder market data specification used in cases when no market data is specification is needed
 */
public final class EmptyMarketDataSpec implements MarketDataSpecification {

  /**
   * Create an instance of the EmptyMarketDataSpec
   */
  public static final EmptyMarketDataSpec INSTANCE = new EmptyMarketDataSpec();

  private EmptyMarketDataSpec() {}

}
