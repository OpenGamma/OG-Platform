/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaltimeseries;

import java.util.Map;

import org.threeten.bp.Duration;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.PublicSPI;

/**
 * Cache storing historical values while the engine is running.
 * <p>
 * The values can be used for graphing. For example, it would be possible to show
 * graphically the historical evolution of fair value on AAPL stock for the
 * past 24 hours, in 1-minute bars.
 */
@PublicSPI
public interface IntradayComputationCache {

  /**
   * Instructs the cache to store historical information at the given resolution.
   * If this resolution already exists, changes numPoints to the given value.
   * 
   * @param resolution  the resolution you want, not null
   * @param numPoints  how many points to store at this resolution, one or greater
   * @throws IllegalArgumentException if numPoints <= 0
   */
  void addResolution(Duration resolution, int numPoints);

  /**
   * Instructs the cache to stop storing historical information at the given resolution.
   * If this resolution does not exist, does nothing.
   * 
   * @param resolution  the resolution you no longer want, not null
   */
  void removeResolution(Duration resolution);

  /**
   * Gets all currently active resolutions.
   * 
   * @return a map of the resolutions to number of points, not null
   */
  Map<Duration, Integer> getResolutions();

  /**
   * Gets all values stored in the intraday cache for the given specification.
   * For example, find fair value on AAPL stock at a 5-minute resolution.
   * <p>
   * Points in the time series before the last point are at a fixed interval
   * (i.e., the resolution, e.g., 5 minutes). However, the last point in the time-series
   * is the result of the very last calculation, so the time difference between the last
   * point and the previous one can be something other than the resolution interval.
   * 
   * @param viewName  the view you want the results for
   * (the computation cache runs within a ViewProcessor, so it may be handling multiple views)
   * @param calcConf  the calculation configuration name, such as 'Default'
   * @param specification  the value specification, such as fair value on AAPL stock, not null
   * @param resolution  the resolution, such as 5-minute resolution, not null
   * @return all values stored in the cache for the given specification at the given resolution,
   *  null if no values are found
   * @throws IllegalArgumentException if the given resolution has not been set up
   */
  ZonedDateTimeDoubleTimeSeries getValue(
      String viewName, 
      String calcConf, 
      ValueSpecification specification, 
      Duration resolution);

}
