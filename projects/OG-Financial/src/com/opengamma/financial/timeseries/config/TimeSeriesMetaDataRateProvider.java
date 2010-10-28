/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import com.opengamma.financial.timeseries.TimeSeriesMetaData;

/**
 * TimeSeriesMetaDataRateProvider, scores the metadata based on rule
 */
public interface TimeSeriesMetaDataRateProvider {
  /**
   * Rates a meta data based on its rules
   * @param metaData the metadata, not-null
   * @return the rating
   */
  int rate(TimeSeriesMetaData metaData);
}
