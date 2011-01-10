/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
public class CornishFisherDeltaGammaVaRCalculator<T> implements Function<T, Double> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private final double _z;
  private final double _mult;
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;
  private final Function<T, Double> _skewCalculator;
  private final Function<T, Double> _kurtosisCalculator;
  private final double _horizon;
  private final double _periods;
  private final double _quantile;

  public CornishFisherDeltaGammaVaRCalculator(final double horizon, final double periods, final double quantile, final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator,
      final Function<T, Double> skewCalculator, final Function<T, Double> kurtosisCalculator) {
    Validate.isTrue(horizon > 0, "horizon");
    Validate.isTrue(periods > 0, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    Validate.notNull(skewCalculator, "skew calculator");
    Validate.notNull(kurtosisCalculator, "kurtosis calculator");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _z = NORMAL.getInverseCDF(quantile);
    _mult = Math.sqrt(horizon / periods);
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
    _skewCalculator = skewCalculator;
    _kurtosisCalculator = kurtosisCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  public Function<T, Double> getSkewCalculator() {
    return _skewCalculator;
  }

  public Function<T, Double> getKurtosisCalculator() {
    return _kurtosisCalculator;
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
    final double zSq = _z * _z;
    final double mean = _meanCalculator.evaluate(data);
    final double std = _stdCalculator.evaluate(data);
    final double skew = _skewCalculator.evaluate(data);
    final double kurtosis = _kurtosisCalculator.evaluate(data);
    final double x = _z + skew * (zSq - 1) / 6. + kurtosis * _z * (zSq - 3) / 24. - skew * skew * _z * (2 * zSq - 5) / 36.;
    return x * std * _mult + mean * _mult * _mult;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _kurtosisCalculator.hashCode();
    result = prime * result + _meanCalculator.hashCode();
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _skewCalculator.hashCode();
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
    final CornishFisherDeltaGammaVaRCalculator<?> other = (CornishFisherDeltaGammaVaRCalculator<?>) obj;
    if (Double.doubleToLongBits(_horizon) != Double.doubleToLongBits(other._horizon)) {
      return false;
    }
    if (Double.doubleToLongBits(_periods) != Double.doubleToLongBits(other._periods)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantile) != Double.doubleToLongBits(other._quantile)) {
      return false;
    }
    if (!ObjectUtils.equals(_kurtosisCalculator, other._kurtosisCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_meanCalculator, other._meanCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_skewCalculator, other._skewCalculator)) {
      return false;
    }
    return ObjectUtils.equals(_stdCalculator, other._stdCalculator);
  }

}
