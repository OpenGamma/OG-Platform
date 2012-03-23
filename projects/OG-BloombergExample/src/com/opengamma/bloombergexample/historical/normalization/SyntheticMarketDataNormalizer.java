/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.historical.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Normalizer for synthetic data used in the example server.
 */
public class SyntheticMarketDataNormalizer implements HistoricalTimeSeriesAdjuster {

  private static final Logger s_logger = LoggerFactory.getLogger(SyntheticMarketDataNormalizer.class);

  @Override
  public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
    final String ticker = securityIdBundle.getValue(SecurityUtils.OG_SYNTHETIC_TICKER);
    
    if (ticker == null) {
      s_logger.warn("Unable to classify security - no synthetic ticker found in {}", securityIdBundle);
      return timeSeries;
    }
    int factor = 0;
    if (ticker.length() > 7) {
      final String type = ticker.substring(3, 7);
      if ("CASH".equals(type) || "SWAP".equals(type) || "LIBO".equals(type)) {
        factor = 100;
      }
    }
    if (factor == 0) {
      s_logger.warn("Unable to classify security - synthetic ticker {} unrecognised", ticker);
      return timeSeries;
    }
    if (factor == 1) {
      s_logger.debug("Returning raw timeseries");
      return timeSeries;
    }
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), (LocalDateDoubleTimeSeries) timeSeries.getTimeSeries().divide(factor));
  }

}
