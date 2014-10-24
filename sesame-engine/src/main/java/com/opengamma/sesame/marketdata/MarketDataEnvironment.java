/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;

/**
 * A market data environment contains all the data required for a single calculation cycle.
 * <p>
 * An environment can contain multiple sets of data, one for each scenario in the calculations.
 *
 * @param <T> the type of the ID used to identify data for different scenarios in the environment.
 */
public interface MarketDataEnvironment<T> {

  /**
   * Returns the data for each scenario, keyed by scenario ID.
   *
   * @return the data for each scenario, keyed by scenario ID
   * @deprecated this method is temporary and will be removed as soon as the engine supports {@code MarketDataBundle}.
   *   At the very least it will be changed to return a map with {@link MarketDataBundle} keys.
   */
  @Deprecated
  Map<T, MapMarketDataBundle> getData();
}
