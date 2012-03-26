/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.historical.normalization;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Mock Implementation of {@code HistoricalTimeSeriesAdjuster} for normalizing time-series data.
 */
public class MockHistoricalTimeSeriesNormalizer implements HistoricalTimeSeriesAdjuster {

  /**
   * The normalization factor.
   */
  private Integer _normalizationFactor = 1;

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries) {
    if (getNormalizationFactor() == 1) {
      return timeSeries;
    }
    LocalDateDoubleTimeSeries normalizedTimeSeries = (LocalDateDoubleTimeSeries) timeSeries.getTimeSeries().divide(getNormalizationFactor());
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), normalizedTimeSeries);
  }

  /**
   * Gets the normalization factor.
   * 
   * @return the normalization factor
   */
  public Integer getNormalizationFactor() {
    return _normalizationFactor;
  }

  /**
   * Sets the normalization factor.
   * 
   * @param normalizationFactor  the normalization factor
   */
  public void setNormalizationFactor(Integer normalizationFactor) {
    _normalizationFactor = normalizationFactor;
  }

}
