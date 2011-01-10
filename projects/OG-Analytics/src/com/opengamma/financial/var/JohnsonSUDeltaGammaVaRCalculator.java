/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T> The type of the data
 */
public class JohnsonSUDeltaGammaVaRCalculator<T> implements Function<T, Double> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final RealSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder();
  private final double _z;
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;
  private final Function<T, Double> _skewCalculator;
  private final Function<T, Double> _kurtosisCalculator;
  private final double _horizon;
  private final double _periods;
  private final double _quantile;

  public JohnsonSUDeltaGammaVaRCalculator(final double horizon, final double periods, final double quantile, final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator,
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
    _z = NORMAL.getInverseCDF(quantile);
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
    _skewCalculator = skewCalculator;
    _kurtosisCalculator = kurtosisCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStdCalculator() {
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
    // TODO if skewness is positive then need to fit to -x and take from upper tail of distribution
    final double k = _kurtosisCalculator.evaluate(data);
    if (k < 0) {
      throw new IllegalArgumentException("Johnson SU distribution cannot be used for data with negative excess kurtosis");
    }
    final double t = _skewCalculator.evaluate(data);
    final double scale = _horizon / _periods;
    final double mu = _meanCalculator.evaluate(data) * scale;
    final double sigma = _stdCalculator.evaluate(data) * Math.sqrt(scale);
    if (t == 0 && k == 0) {
      return _z * sigma - mu;
    }
    final double wUpper = Math.sqrt(Math.sqrt(2 * (k + 2)) - 1);
    final double wLower = Math.max(getW0(t), getW1(k + 3));
    final double w = ROOT_FINDER.getRoot(getFunction(t, k), wLower, wUpper);
    final double w2 = w * w;
    final double l = 4 + 2 * (w2 - (k + 6) / (w2 + 2 * w + 3));
    if (l < 0) {
      throw new IllegalArgumentException("Tried to find the square root of a negative number");
    }
    final double m = -2 + Math.sqrt(l);
    if (m == 0 || (m < 0 && w > -1) || (m > 0 && w < -1) || (w - 1 - m) < 0) {
      throw new IllegalArgumentException("Invalid parameters");
    }
    final double sign = Math.signum(t);
    final double u = Math.sqrt(Math.log(w));
    final double v = Math.sqrt((w + 1) * (w - 1 - m) / (2 * w * m));
    final double omega = -sign * TrigonometricFunctionUtils.asinh(v).doubleValue();
    final double delta = 1. / u;
    final double gamma = omega / u;
    final double lambda = sigma / (w - 1) * Math.sqrt(2 * m / (w + 1));
    final double ksi = mu - sign * sigma * Math.sqrt(w - 1 - m) / (w - 1);
    return -lambda * Math.sinh((-_z - gamma) / delta) - ksi;
  }

  private double getW0(final double t) {
    final double q = -2 - t * t;
    final double u = Math.cbrt(-q / 2. + Math.sqrt(q * q / 4. - 1));
    return -1. / u + u - 1;
  }

  private double getW1(final double k) {
    final double k2 = 2 * k;
    final double e = 2 * Math.sqrt((3 + k) * (16 * k * k + 87 * k + 171) / 27.);
    final double d = Math.cbrt(7 + k2 + e) - Math.cbrt(e - 7 - k2) - 1;
    final double sqrtD = Math.sqrt(d);
    return (sqrtD + Math.sqrt(4. / sqrtD - d - 3) - 1) / 2.;
  }

  private Function1D<Double, Double> getFunction(final double t, final double k) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double w) {
        final double w2 = w * w;
        final double m = -2 + Math.sqrt(4 + 2 * (w2 - (k + 6) / (w2 + 2 * w + 3)));
        return (w - 1 - m) * Math.pow(w + 2 + m / 2., 2) - t * t;
      }

    };
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
    final JohnsonSUDeltaGammaVaRCalculator<?> other = (JohnsonSUDeltaGammaVaRCalculator<?>) obj;
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
