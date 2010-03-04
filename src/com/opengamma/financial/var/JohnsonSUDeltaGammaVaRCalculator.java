/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.ComplexMath;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class JohnsonSUDeltaGammaVaRCalculator extends VaRCalculator<SkewKurtosisStatistics<?>> {
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);
  private final RealSingleRootFinder _rootFinder = new BisectionSingleRootFinder();
  private double _z;

  public JohnsonSUDeltaGammaVaRCalculator(final double horizon, final double periods, final double quantile) {
    super(horizon, periods, quantile);
    _z = _normal.getInverseCDF(quantile);
  }

  @Override
  public void setQuantile(final double quantile) {
    super.setQuantile(quantile);
    _z = _normal.getInverseCDF(quantile);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final SkewKurtosisStatistics<?> data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    // TODO if skewness is positive then need to fit to -x and take from upper
    // tail of distribution
    final double t = data.getSkew();
    final double k = data.getKurtosis();
    if (k < 0)
      throw new IllegalArgumentException("Johnson SU distribution cannot be used for data with negative excess kurtosis");
    final double scale = getHorizon() / getPeriods();
    final double mu = data.getMean() * scale;
    final double sigma = data.getStandardDeviation() * Math.sqrt(scale);
    if (t == 0 && k == 0)
      return _z * sigma - mu;
    final double wUpper = Math.sqrt(Math.sqrt(2 * (k + 2)) - 1);
    final double wLower = Math.max(getW0(t), getW1(k + 3));
    final double w = _rootFinder.getRoot(getFunction(t, k), wLower, wUpper);
    final double w2 = w * w;
    final double l = 4 + 2 * (w2 - (k + 6) / (w2 + 2 * w + 3));
    if (l < 0)
      throw new IllegalArgumentException("Tried to find the square root of a negative number");
    final double m = -2 + Math.sqrt(l);
    if (m == 0 || (m < 0 && w > -1) || (m > 0 && w < -1) || (w - 1 - m) < 0)
      throw new IllegalArgumentException("Invalid parameters");
    final double sign = Math.signum(t);
    final double u = Math.sqrt(Math.log(w));
    final double v = Math.sqrt((w + 1) * (w - 1 - m) / (2 * w * m));
    final double omega = -sign * ComplexMath.asinh(v).doubleValue();
    final double delta = 1. / u;
    final double gamma = omega / u;
    final double lambda = sigma / (w - 1) * Math.sqrt(2 * m / (w + 1));
    final double ksi = mu - sign * sigma * Math.sqrt(w - 1 - m) / (w - 1);
    return -lambda * Math.sinh((-_z - gamma) / delta) - ksi;
  }

  protected double getW0(final double t) {
    final double q = -2 - t * t;
    final double u = Math.cbrt(-q / 2. + Math.sqrt(q * q / 4. - 1));
    return -1. / u + u - 1;
  }

  protected double getW1(final double k) {
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

}
