/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import com.opengamma.master.timeseries.TimeSeriesInfo;

/**
 * Scores time-series info.
 * <p>
 * This strategy pattern interface allows different rules to be provided for choosing
 * time-series info.
 */
public interface TimeSeriesInfoRateProvider {

  /**
   * Rates time-series info based on its rules.
   * 
   * @param info  the time-series info, not null
   * @return the rating
   */
  int rate(TimeSeriesInfo info);

}
