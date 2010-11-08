/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.util.Map;

import javax.time.Duration;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;

/**
 * This cache stores historical values while the engine is running. The values 
 * can be used for graphing, for example to show graphically the historical evolution 
 * of FV on AAPL stock for the past 24 hours, in 1-minute bars.
 */
@PublicSPI
public interface IntradayComputationCache {
  
  /**
   * Instructs the cache to store historical information at the given resolution.
   * If this resolution already exists, changes numPoints to the given value.
   * 
   * @param resolution The resolution you want, not null
   * @param numPoints How many points to store at this resolution.
   * @throws IllegalArgumentException If numPoints <= 0
   */
  void addResolution(Duration resolution, int numPoints);
  
  /**
   * Instructs the cache to stop storing historical information
   * at the given resolution.
   * If this resolution does not exist, does nothing.
   * 
   * @param resolution The resolution you no longer want, not null
   */
  void removeResolution(Duration resolution);
  
  /**
   * Gets all currently active resolutions.
   * 
   * @return Map telling, for each active resolution, how many
   * points to store at that resolution 
   */
  Map<Duration, Integer> getResolutions();
  
  /**
   * Gets all values stored in the intraday cache for the given 
   * value (e.g., FV on AAPL stock), at the given resolution
   * (e.g., 5 minutes).
   * <p>
   * Points in the time series before the last point are at a fixed
   * interval (i.e., the resolution, e.g., 5 minutes). However,
   * the last point in the time series is the result of the
   * very last calculation, so the time difference between the last
   * point and the previous one can be something other than the
   * resolution interval.
   * 
   * @param viewName Which view you want the results for
   * (the computation cache runs within a ViewProcessor,
   * so it may be handling multiple views)
   * @param calcConf Calculation configuration name, e.g., Default
   * @param specification E.g., FV on AAPL stock, not null
   * @param resolution E.g., 5-minute resolution, not null
   * @return All values stored in the cache for the given spec
   * at the given resolution. {@code null} if no values are
   * found for the given spec.
   * @throws IllegalArgumentException If the given resolution
   * has not been set up.
   */
  DateTimeDoubleTimeSeries getValue(
      String viewName, 
      String calcConf, 
      ValueSpecification specification, 
      Duration resolution);

}
