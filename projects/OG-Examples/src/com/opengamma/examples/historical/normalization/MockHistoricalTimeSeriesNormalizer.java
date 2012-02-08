/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.historical.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Mock Implementation of {@link HistoricalTimeSeriesAdjuster} for normalizing time-series data.
 * 
 */
public class MockHistoricalTimeSeriesNormalizer implements HistoricalTimeSeriesAdjuster {
  
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(MockHistoricalTimeSeriesNormalizer.class);

  private Integer _normalizationFactor = 1;
  
  @Override
  public HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries) {
    
    if (getNormalizationFactor() == 1) {
      return timeSeries;
    }
    LocalDateDoubleTimeSeries normalizedTimeSeries = (LocalDateDoubleTimeSeries) timeSeries.getTimeSeries().divide(getNormalizationFactor());
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), normalizedTimeSeries);
  }

  /**
   * Gets the normalizationFactor.
   * @return the normalizationFactor
   */
  public Integer getNormalizationFactor() {
    return _normalizationFactor;
  }

  /**
   * Sets the normalizationFactor.
   * @param normalizationFactor  the normalizationFactor
   */
  public void setNormalizationFactor(Integer normalizationFactor) {
    _normalizationFactor = normalizationFactor;
  }
    
}
