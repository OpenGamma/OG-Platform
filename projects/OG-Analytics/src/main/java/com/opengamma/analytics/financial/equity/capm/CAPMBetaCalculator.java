/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.capm;

import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesDates;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesSize;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class CAPMBetaCalculator implements Function<DoubleTimeSeries<?>, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(CAPMBetaCalculator.class);
  private final DoubleTimeSeriesStatisticsCalculator _covarianceCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _varianceCalculator;

  //TODO switch to CovarianceCalculator 
  public CAPMBetaCalculator(final DoubleTimeSeriesStatisticsCalculator covarianceCalculator, final DoubleTimeSeriesStatisticsCalculator varianceCalculator) {
    Validate.notNull(covarianceCalculator, "covariance calculator");
    Validate.notNull(varianceCalculator, "variance calculator");
    _covarianceCalculator = covarianceCalculator;
    _varianceCalculator = varianceCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... ts) {
    Validate.notNull(ts, "ts");
    final int n = ts.length;
    Validate.isTrue(n > 1);
    if (n > 3) {
      s_logger.warn("Found more than two time series; will only use the first two");
    }
    final DoubleTimeSeries<?> assetReturn = ts[0];
    final DoubleTimeSeries<?> marketReturn = ts[1];
    testTimeSeriesSize(assetReturn, 2);
    testTimeSeriesSize(marketReturn, 2);
    testTimeSeriesDates(assetReturn, marketReturn);
    return _covarianceCalculator.evaluate(assetReturn, marketReturn) / _varianceCalculator.evaluate(marketReturn);
  }

}
