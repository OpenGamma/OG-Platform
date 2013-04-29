/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.capm;

import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testNotNullOrEmpty;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesDates;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesSize;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.analytics.math.regression.OrdinaryLeastSquaresRegression;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 *
 */
public class CAPMFromRegressionCalculator implements Function<DoubleTimeSeries<?>, LeastSquaresRegressionResult> {
  private static final Logger s_logger = LoggerFactory.getLogger(CAPMFromRegressionCalculator.class);
  private static final OrdinaryLeastSquaresRegression OLS = new OrdinaryLeastSquaresRegression();

  /**
   * @param ts The first argument is the return time series of the asset, the second is the return time series of the market.
   * @return The regression result
   */
  @Override
  public LeastSquaresRegressionResult evaluate(final DoubleTimeSeries<?>... ts) {
    Validate.notNull(ts, "ts");
    final int n = ts.length;
    Validate.isTrue(n > 1);
    if (n > 2) {
      s_logger.warn("Found more than two time series; will only use the first two");
    }
    final DoubleTimeSeries<?> assetTS = ts[0];
    final DoubleTimeSeries<?> marketTS = ts[1];
    testNotNullOrEmpty(assetTS);
    testNotNullOrEmpty(marketTS);
    testTimeSeriesSize(assetTS, 2);
    testTimeSeriesSize(marketTS, 2);
    testTimeSeriesDates(assetTS, marketTS);
    final double[] asset = assetTS.valuesArrayFast();
    final double[][] market = new double[asset.length][1];
    for (int i = 0; i < asset.length; i++) {
      market[i][0] = marketTS.getValueAtIndex(i);
    }
    //final double[][] market = new double[][] {marketTS.toFastLongDoubleTimeSeries().valuesArrayFast()}; //TODO change when [ANA-254] is completed
    final LeastSquaresRegressionResult result = OLS.regress(market, asset, true);
    return result;
  }
}
