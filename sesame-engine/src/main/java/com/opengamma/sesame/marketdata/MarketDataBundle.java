/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Provides market data to functions.
 * <p>
 * In this context, market data can be a high-level object (e.g. a calibrated curve), a single simple value
 * (e.g. the market price of a security), or a time series of values.
 * <p>
 * Market data returned from the {@link #get} method is associated with a specific time, the <em>market data time</em>,
 * which is a property of a market data bundle instance. The default market data bundle created by the
 * engine has its market data time set to the valuation time. If a function needs market data for a different
 * time it should use:
 *
 * <pre>
 *   marketDataBundle.withTime(...).get(key);
 * </pre>
 *
 * If a function needs to call another function, and have that function use market data from a different date,
 * it can create a new market data bundle and pass it to the function:
 *
 * <pre>
 *   MarketDataBundle newMarketDataBundle = marketDataBundle.withTime(...);
 *   Environment newEnv = env.withMarketDataBundle(newMarketDataBundle);
 *   Result result = childFn.doSomething(newEnv, ...);
 * </pre>
 */
public interface MarketDataBundle {

  /**
   * Gets an item of market data from the bundle.
   * <p>
   * The value of the market data is associated with the <em>market data time</em> of the bundle.
   *
   * @param id the ID that identifies the market data item
   * @param dataType the expected type of the market data
   * @return a successful result containing the market data, or a failure result if it is unavailable or not
   *   compatible with the requested type
   *
   * TODO is the dataType arg necessary? shouldn't the id's type be enough?
   */
  <T> Result<T> get(MarketDataId<?> id, Class<T> dataType);

  /**
   * Returns a time series of market data values.
   *
   * @param id the ID identifying the market data
   * @param dateRange the date range for which market data is required
   * @param dataType the expected type of the market data
   * @return a successful result containing the market data, or a failure result if it is unavailable or not
   *   compatible with the requested type
   */
  <T> Result<DateTimeSeries<LocalDate, T>> get(MarketDataId<?> id, Class<T> dataType, LocalDateRange dateRange);

  /**
   * Returns a market data bundle containing data for the specified time.
   *
   * @param time the time
   * @return an bundle containing data for the time
   */
  MarketDataBundle withTime(ZonedDateTime time);

  /**
   * Returns a market data bundle containing data for the specified date.
   *
   * @param date the date
   * @return an bundle containing data for the date
   */
  MarketDataBundle withDate(LocalDate date);
}
