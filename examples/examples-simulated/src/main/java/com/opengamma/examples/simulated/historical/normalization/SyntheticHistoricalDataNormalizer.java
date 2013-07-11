/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical.normalization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;

/**
 * Normalizer for synthetic data used in the example server.
 */
public class SyntheticHistoricalDataNormalizer implements HistoricalTimeSeriesAdjuster {

  private static final Logger s_logger = LoggerFactory.getLogger(SyntheticHistoricalDataNormalizer.class);

  private static final Integer HUNDRED = 100;
  private static final Integer ONE = 1;

  private static final Pattern s_rates = Pattern.compile("[A-Z]{3}(CASH|SWAP|LIBOR|EURIBOR|OIS_SWAP|FRA|BB|BASIS_SWAP_.*)P[0-9]+[DMY]");
  private static final Pattern s_futureRates = Pattern.compile("ER(H|M|U|Z)[0-9]{2}");
  private final ConcurrentMap<String, Integer> _factors = new ConcurrentHashMap<>();

  public SyntheticHistoricalDataNormalizer() {
    // Overnight rates (don't match the regex pattern looked for)
    _factors.put("USDFF", HUNDRED);
    _factors.put("EONIA", HUNDRED);
    _factors.put("SONIO", HUNDRED);
    _factors.put("TONAR", HUNDRED);
    _factors.put("TOISTOIS", HUNDRED);
    _factors.put("AUDON", HUNDRED);
    _factors.put("ER", HUNDRED);
  }

  private int getFactor(final ExternalIdBundle securityIdBundle) {
    final String ticker = securityIdBundle.getValue(ExternalSchemes.OG_SYNTHETIC_TICKER);
    if (ticker == null) {
      s_logger.warn("Unable to classify security - no synthetic ticker found in {}", securityIdBundle);
      return 1;
    }
    final Integer factor = _factors.get(ticker);
    if (factor != null) {
      return factor.intValue();
    }
    Matcher matcher = s_rates.matcher(ticker);
    if (matcher.matches()) {
      s_logger.info("Using 100 for ticker {}", ticker);
      _factors.putIfAbsent(ticker, HUNDRED);
      return 100;
    }
    matcher = s_futureRates.matcher(ticker);
    if (matcher.matches()) {
      s_logger.info("Using 100 for ticker {}", ticker);
      _factors.putIfAbsent(ticker, HUNDRED);
      return 100;
    }
    s_logger.info("Assuming 1 for ticker {}", ticker);
    _factors.putIfAbsent(ticker, ONE);
    return 1;
  }

  @Override
  public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
    final int factor = getFactor(securityIdBundle);
    if (factor == 1) {
      s_logger.debug("Returning raw timeseries");
      return timeSeries;
    }
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Dividing timeseries by {}", factor);
    }
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), timeSeries.getTimeSeries().divide(factor));
  }

  @Override
  public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
    final int factor = getFactor(securityIdBundle);
    if (factor == 1) {
      return HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    }
    return new HistoricalTimeSeriesAdjustment.DivideBy(factor);
  }

}
