/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class PNLStatisticsCalculator extends Function1D<HistoricalVaRDataBundle, Double> {
  private final Function1D<DoubleTimeSeries, Double> _calculator;

  public PNLStatisticsCalculator(final Function1D<DoubleTimeSeries, Double> calculator) {
    _calculator = calculator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final HistoricalVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    return _calculator.evaluate(data.getPNLSeries());
  }

}
