/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class PNLMeanCalculator extends Function1D<HistoricalVaRDataBundle, Double> {
  private final Function1D<DoubleTimeSeries, Double> _mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final HistoricalVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    return _mean.evaluate(data.getPNLSeries());
  }
}
