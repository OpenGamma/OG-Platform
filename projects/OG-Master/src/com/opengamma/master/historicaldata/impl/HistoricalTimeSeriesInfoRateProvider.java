/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import com.opengamma.master.historicaldata.HistoricalTimeSeriesInfo;

/**
 * Scores historical time-series info.
 * <p>
 * This strategy pattern interface allows different rules to be provided for choosing
 * historical time-series info.
 */
public interface HistoricalTimeSeriesInfoRateProvider {

  /**
   * Rates historical time-series info based on its rules.
   * 
   * @param info  the time-series info, not null
   * @return the rating
   */
  int rate(HistoricalTimeSeriesInfo info);

}
