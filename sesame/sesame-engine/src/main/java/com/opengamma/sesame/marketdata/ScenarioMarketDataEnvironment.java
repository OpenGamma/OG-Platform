/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;

/**
 * An environment containing the market data for running a set of scenarios.
 * <p>
 * An environment contains multiple sets of data, one for each scenario in the calculations.
 */
public interface ScenarioMarketDataEnvironment {

  /**
   * Returns the data for each scenario, keyed by scenario ID.
   *
   * @return the data for each scenario, keyed by scenario ID
   */
  Map<String, MarketDataEnvironment> getData();
}
