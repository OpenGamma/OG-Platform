/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.util.PublicAPI;

/**
 * Standard field names for historical time-series.
 * <p>
 * A historical time-series represents a certain type of data.
 * The field name used to retrieve the data will be different on each data source.
 * This class provides standard normalized field names valid across different data sources.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class HistoricalTimeSeriesFields {
  // only add constants here that might reasonably be retrieved from multiple data sources
  // always use a generic name rather than a data source specific name

  /**
   * A time-series representing the last price.
   * This is the end of day price historically and the latest price today.
   */
  public static final String LAST_PRICE = "PX_LAST";
  /**
   * A time-series representing traded volume.
   */
  public static final String VOLUME = "VOLUME";
  /**
   * A time-series representing yield-to-maturity (mid).
   */
  public static final String YIELD_TO_MATURITY = "YIELD_TO_MATURITY";
  /**
   * A time-series representing yield-to-convention (mid).
   */
  public static final String YIELD_TO_CONVENTION = "YIELD_TO_CONVENTION";

  /**
   * Restricted constructor.
   * This class can be sub-classed to add application constants.
   */
  protected HistoricalTimeSeriesFields() {
  }

}
