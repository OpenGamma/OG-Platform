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
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Normalizer for synthetic data used in the example server.
 */
public class SyntheticMarketDataNormalizer implements HistoricalTimeSeriesAdjuster {

  private static final Logger s_logger = LoggerFactory.getLogger(SyntheticMarketDataNormalizer.class);

  private int getFactor(final ExternalIdBundle securityIdBundle) {
    final String ticker = securityIdBundle.getValue(ExternalSchemes.OG_SYNTHETIC_TICKER);
    if (ticker == null) {
      s_logger.warn("Unable to classify security - no synthetic ticker found in {}", securityIdBundle);
      return 1;
    }
    int factor = 0;
    if (ticker.length() > 7) {
      final String type = ticker.substring(3, 7);
      if ("CASH".equals(type) || "SWAP".equals(type) || "LIBO".equals(type)) {
        factor = 100;
      }
    }
    if (factor == 0) {
      s_logger.debug("Unable to classify security - synthetic ticker {} unrecognised", ticker);
      return 1;
    }
    return factor;
  }

  @Override
  public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
    int factor = getFactor(securityIdBundle);
    if (factor == 1) {
      s_logger.debug("Returning raw timeseries");
      return timeSeries;
    }
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), (LocalDateDoubleTimeSeries) timeSeries.getTimeSeries().divide(factor));
  }

  @Override
  public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
    int factor = getFactor(securityIdBundle);
    if (factor == 1) {
      return HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    }
    return new HistoricalTimeSeriesAdjustment.DivideBy(factor);
  }

}
