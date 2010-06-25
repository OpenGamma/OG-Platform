/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
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

  @Override
  public Double evaluate(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    final double[] data = ts.toFastLongDoubleTimeSeries().valuesArrayFast();
    Arrays.sort(data);
    return _mult * _percentileCalculator.evaluate(data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_mult);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_percentileCalculator == null) ? 0 : _percentileCalculator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EmpiricalDistributionVaRCalculator other = (EmpiricalDistributionVaRCalculator) obj;
    if (Double.doubleToLongBits(_mult) != Double.doubleToLongBits(other._mult)) {
      return false;
    }
    if (_percentileCalculator == null) {
      if (other._percentileCalculator != null) {
        return false;
      }
    } else if (!_percentileCalculator.equals(other._percentileCalculator)) {
      return false;
    }
    return true;
  }

}
