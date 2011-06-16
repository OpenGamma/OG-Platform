/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import com.opengamma.master.historicaldata.HistoricalDataInfo;

/**
 * Scores historical data info.
 * <p>
 * This strategy pattern interface allows different rules to be provided for choosing
 * historical data info.
 */
public interface HistoricalDataInfoRateProvider {

  /**
   * Rates historical data info based on its rules.
   * 
   * @param info  the time-series info, not null
   * @return the rating
   */
  int rate(HistoricalDataInfo info);

}
