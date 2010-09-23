/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import static com.opengamma.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesDates;
import static com.opengamma.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesSize;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.math.function.Function;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class CAPMBetaCalculator implements Function<DoubleTimeSeries<?>, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(CAPMBetaCalculator.class);
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _covarianceCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _varianceCalculator;

  //TODO switch to CovarianceCalculator 
  public CAPMBetaCalculator(final TimeSeriesReturnCalculator returnCalculator, final DoubleTimeSeriesStatisticsCalculator covarianceCalculator,
      final DoubleTimeSeriesStatisticsCalculator varianceCalculator) {
    Validate.notNull(returnCalculator, "return series calculator");
    Validate.notNull(covarianceCalculator, "covariance calculator");
    Validate.notNull(varianceCalculator, "variance calculator");
    _returnCalculator = returnCalculator;
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
    final DoubleTimeSeries<?> assetTS = ts[0];
    final DoubleTimeSeries<?> marketTS = ts[1];
    testTimeSeriesSize(assetTS, 2);
    testTimeSeriesSize(marketTS, 2);
    testTimeSeriesDates(assetTS, marketTS);
    final DoubleTimeSeries<?> assetReturn = _returnCalculator.evaluate(assetTS);
    final DoubleTimeSeries<?> marketReturn = _returnCalculator.evaluate(marketTS);
    return _covarianceCalculator.evaluate(assetReturn, marketReturn) / _varianceCalculator.evaluate(marketReturn);
  }

}
