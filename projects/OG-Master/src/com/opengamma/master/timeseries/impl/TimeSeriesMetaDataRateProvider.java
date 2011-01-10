/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import com.opengamma.master.timeseries.TimeSeriesMetaData;

/**
 * Scores a meta-data instance.
 * <p>
 * This strategy pattern interface allows different rules to be provided for choosing
 * time-series meta-data.
 */
public interface TimeSeriesMetaDataRateProvider {

  /**
   * Rates a meta data based on its rules.
   * 
   * @param metaData  the meta-data, not null
   * @return the rating
   */
  int rate(TimeSeriesMetaData metaData);

}
