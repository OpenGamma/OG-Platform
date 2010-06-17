/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 *
 */
public class EmpiricalDistributionVaRCalculator extends VaRCalculator<DoubleTimeSeries<?>> {
  private double _mult;
  private Function1D<double[], Double> _percentileCalculator;

  public EmpiricalDistributionVaRCalculator(final double horizon, final double periods, final double quantile) {
    super(horizon, periods, quantile);
    _percentileCalculator = new PercentileCalculator(1 - quantile);
    setMultiplier();
  }

  @Override
  public void setHorizon(final double horizon) {
    super.setHorizon(horizon);
    setMultiplier();
  }

  @Override
  public void setPeriods(final double periods) {
    super.setPeriods(periods);
    setMultiplier();
  }

  @Override
  public void setQuantile(final double quantile) {
    super.setQuantile(quantile);
    _percentileCalculator = new PercentileCalculator(1 - quantile);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt(getHorizon() / getPeriods());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final DoubleTimeSeries<?> ts) {
    if (ts == null) {
      throw new IllegalArgumentException("Time series was null");
    }
    final double[] data = ts.toFastLongDoubleTimeSeries().valuesArrayFast();
    Arrays.sort(data);
    return _mult * _percentileCalculator.evaluate(data);
  }

}
