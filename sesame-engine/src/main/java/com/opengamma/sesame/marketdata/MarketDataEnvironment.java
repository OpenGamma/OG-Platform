/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.date.DateTimeSeries;

/**
 * Contains market data for use in a set of calculations.
 * <p>
 * A {@code MarketDataEnvironment} can only contain a single instance of a piece of market data. e.g. it can
 * only contain one curve with a particular name or one time series for a particular ticker.
 * Use {@link ScenarioMarketDataEnvironment} to hold multiple copies of data for use when running multiple scenarios.
 */
public interface MarketDataEnvironment {

  /**
   * @return single market data values, keyed by the requirement used to request them
   */
  Map<MarketDataRequirement, Object> getData();

  /**
   * @return time series of market data values, keyed by the ID of the values in the time series.
   */
  Map<MarketDataId, DateTimeSeries<LocalDate, ?>> getTimeSeries();

  /**
   * @return the valuation time of the market data
   */
  ZonedDateTime getValuationTime();

  /**
   * @return a builder created from the data in this environment
   */
  MarketDataEnvironmentBuilder toBuilder();
}
