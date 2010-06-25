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
public class CornishFisherDeltaGammaVaRCalculator extends VaRCalculator<SkewKurtosisStatistics<?>> {
  private double _z;
  private double _mult;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public CornishFisherDeltaGammaVaRCalculator(final double horizon, final double periods, final double quantile) {
    super(horizon, periods, quantile);
    _z = _normal.getInverseCDF(quantile);
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
    _z = _normal.getInverseCDF(quantile);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt(getHorizon() / getPeriods());
  }

  @Override
  public Double evaluate(final SkewKurtosisStatistics<?> statistics) {
    Validate.notNull(statistics, "statistics");
    final double zSq = _z * _z;
    final double mean = statistics.getMean();
    final double std = statistics.getStandardDeviation();
    final double skew = statistics.getSkew();
    final double kurtosis = statistics.getKurtosis();
    final double x = _z + skew * (zSq - 1) / 6. + kurtosis * _z * (zSq - 3) / 24. - skew * skew * _z * (2 * zSq - 5) / 36.;
    return x * std * _mult + mean * _mult * _mult;
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
    CornishFisherDeltaGammaVaRCalculator other = (CornishFisherDeltaGammaVaRCalculator) obj;
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
