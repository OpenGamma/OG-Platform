/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.historical.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.id.ExternalIdBundle;

/**
 * Implementation of {@link HistoricalTimeSeriesAdjuster} for normalizing time-series consisting of Bloomberg market
 * data. Ignores security type - applies fixed normalization.
 */
public class BloombergFixedRateHistoricalTimeSeriesNormalizer implements HistoricalTimeSeriesAdjuster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergFixedRateHistoricalTimeSeriesNormalizer.class);

  private final HistoricalTimeSeriesAdjustment _normalization;

  public BloombergFixedRateHistoricalTimeSeriesNormalizer(final HistoricalTimeSeriesAdjustment normalization) {
    _normalization = normalization;
  }

  @Override
  public HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries) {
    return _normalization.adjust(timeSeries);
  }

  @Override
  public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
    return _normalization;
  }

}
