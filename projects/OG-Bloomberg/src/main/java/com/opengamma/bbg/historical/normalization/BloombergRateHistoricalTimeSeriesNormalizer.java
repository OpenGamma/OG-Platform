/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.historical.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.normalization.BloombergRateClassifier;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Implementation of {@link HistoricalTimeSeriesAdjuster} for normalizing time-series consisting of Bloomberg market
 * data.
 */
public class BloombergRateHistoricalTimeSeriesNormalizer implements HistoricalTimeSeriesAdjuster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergRateHistoricalTimeSeriesNormalizer.class);

  private final BloombergRateClassifier _classifier;
  
  public BloombergRateHistoricalTimeSeriesNormalizer(BloombergRateClassifier classifier) {
    _classifier = classifier;
  }

  protected BloombergRateClassifier getClassifier() {
    return _classifier;
  }

  protected Integer getNormalizationFactor(final ExternalIdBundle securityIdBundle) {
    String buid = securityIdBundle.getValue(ExternalSchemes.BLOOMBERG_BUID);
    if (buid == null) {
      s_logger.warn("Unable to classify security for Bloomberg time-series normalization as no BUID found in bundle: {}. The time-series will be unnormalized.", securityIdBundle);
      return null;
    }
    Integer normalizationFactor = getClassifier().getNormalizationFactor(buid);
    if (normalizationFactor == null) {
      s_logger.warn("Unable to classify security for Bloomberg time-series normalization: {}. The time-series will be unnormalized.", securityIdBundle);
      return null;
    }
    if (normalizationFactor == 1) {
      return null;
    }
    return normalizationFactor;
  }

  @Override
  public HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries) {
    final Integer normalizationFactor = getNormalizationFactor(securityIdBundle);
    if (normalizationFactor == null) {
      return timeSeries;
    }
    LocalDateDoubleTimeSeries normalizedTimeSeries = (LocalDateDoubleTimeSeries) timeSeries.getTimeSeries().divide(normalizationFactor);
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), normalizedTimeSeries);
  }
  
  @Override
  public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
    Integer normalizationFactor = getNormalizationFactor(securityIdBundle);
    if (normalizationFactor == null) {
      return HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    }
    return new HistoricalTimeSeriesAdjustment.DivideBy(normalizationFactor);
  }

}
