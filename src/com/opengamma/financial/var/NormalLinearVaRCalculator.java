/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class NormalLinearVaRCalculator extends VaRCalculator<NormalStatistics<?>> {
  private double _mult;
  private double _z;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public NormalLinearVaRCalculator(final double horizon, final double periods, final double quantile) {
    super(horizon, periods, quantile);
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
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt(getHorizon() / getPeriods());
    _z = _normal.getInverseCDF(getQuantile());
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    Validate.notNull(statistics, "statistics");
    return _z * _mult * statistics.getStandardDeviation() - _mult * _mult * statistics.getMean();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_mult);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_normal == null) ? 0 : _normal.hashCode());
    temp = Double.doubleToLongBits(_z);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    NormalLinearVaRCalculator other = (NormalLinearVaRCalculator) obj;
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
    if (Double.doubleToLongBits(_z) != Double.doubleToLongBits(other._z)) {
      return false;
    }
    return true;
  }

}
