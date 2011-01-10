/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T> The type of the data
 */
public class NormalLinearVaRCalculator<T> implements Function<T, Double> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private final double _mult;
  private final double _z;
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;
  private final double _horizon;
  private final double _periods;
  private final double _quantile;

  public NormalLinearVaRCalculator(final double horizon, final double periods, final double quantile, final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator) {
    Validate.isTrue(horizon > 0, "horizon");
    Validate.isTrue(periods > 0, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _z = NORMAL.getInverseCDF(quantile);
    _mult = Math.sqrt(horizon / periods);
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  public double getHorizon() {
    return _horizon;
  }

  public double getPeriods() {
    return _periods;
  }

  public double getQuantile() {
    return _quantile;
  }

  @Override
  public Double evaluate(final T... data) {
    Validate.notNull(data, "data");
    return _z * _mult * _stdCalculator.evaluate(data) - _mult * _mult * _meanCalculator.evaluate(data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _meanCalculator.hashCode();
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _stdCalculator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NormalLinearVaRCalculator<?> other = (NormalLinearVaRCalculator<?>) obj;
    if (Double.doubleToLongBits(_horizon) != Double.doubleToLongBits(other._horizon)) {
      return false;
    }
    if (Double.doubleToLongBits(_periods) != Double.doubleToLongBits(other._periods)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantile) != Double.doubleToLongBits(other._quantile)) {
      return false;
    }
    return ObjectUtils.equals(_meanCalculator, other._meanCalculator) && ObjectUtils.equals(_stdCalculator, other._stdCalculator);
  }

}
