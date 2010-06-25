/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.conditional;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.var.NormalStatistics;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NormalLinearConditionalVaRCalculator extends Function1D<NormalStatistics<?>, Double> {
  private double _horizon;
  private double _periods;
  private double _quantile;
  private double _mult;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public NormalLinearConditionalVaRCalculator(final double horizon, final double periods, final double quantile) {
    ArgumentChecker.notNegative(horizon, "horizon");
    ArgumentChecker.notNegative(periods, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    setMultiplier();
  }

  public void setHorizon(final double horizon) {
    ArgumentChecker.notNegative(horizon, "horizon");
    _horizon = horizon;
    setMultiplier();
  }

  public void setPeriods(final double periods) {
    ArgumentChecker.notNegative(periods, "periods");
    _periods = periods;
    setMultiplier();
  }

  public void setQuantile(final double quantile) {
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    _quantile = quantile;
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = _normal.getPDF(_normal.getInverseCDF(_quantile)) * Math.sqrt(_horizon / _periods) / (1 - _quantile);
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    Validate.notNull(statistics, "statistics");
    return _mult * statistics.getStandardDeviation();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_mult);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_normal == null) ? 0 : _normal.hashCode());
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NormalLinearConditionalVaRCalculator other = (NormalLinearConditionalVaRCalculator) obj;
    if (Double.doubleToLongBits(_horizon) != Double.doubleToLongBits(other._horizon)) {
      return false;
    }
    if (Double.doubleToLongBits(_mult) != Double.doubleToLongBits(other._mult)) {
      return false;
    }
    if (_normal == null) {
      if (other._normal != null) {
        return false;
      }
    } else if (!_normal.equals(other._normal)) {
      return false;
    }
    if (Double.doubleToLongBits(_periods) != Double.doubleToLongBits(other._periods)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantile) != Double.doubleToLongBits(other._quantile)) {
      return false;
    }
    return true;
  }

}
