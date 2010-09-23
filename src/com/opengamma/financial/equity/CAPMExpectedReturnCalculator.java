/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.util.TimeSeriesDataTestUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class CAPMExpectedReturnCalculator {
  private final TimeSeriesReturnCalculator _marketReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedMarketReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedRiskFreeRateCalculator;

  public CAPMExpectedReturnCalculator(final TimeSeriesReturnCalculator marketReturnCalculator, final DoubleTimeSeriesStatisticsCalculator expectedMarketReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator expectedRiskFreeRateCalculator) {
    Validate.notNull(marketReturnCalculator, "market return series calculator");
    Validate.notNull(expectedMarketReturnCalculator, "expected market return calculator");
    Validate.notNull(expectedRiskFreeRateCalculator, "expected risk free rate calculator");
    _marketReturnCalculator = marketReturnCalculator;
    _expectedMarketReturnCalculator = expectedMarketReturnCalculator;
    _expectedRiskFreeRateCalculator = expectedRiskFreeRateCalculator;
  }

  public Double evaluate(final DoubleTimeSeries<?> marketPriceTS, final DoubleTimeSeries<?> riskFreeRateTS, final double beta) {
    TimeSeriesDataTestUtils.testNotNullOrEmpty(marketPriceTS);
    TimeSeriesDataTestUtils.testNotNullOrEmpty(riskFreeRateTS);
    final Double expectedMarketReturn = _expectedMarketReturnCalculator.evaluate(_marketReturnCalculator.evaluate(marketPriceTS));
    final Double expectedRiskFreeReturn = _expectedRiskFreeRateCalculator.evaluate(riskFreeRateTS);
    return expectedRiskFreeReturn + beta * (expectedMarketReturn - expectedRiskFreeReturn);
  }

}
