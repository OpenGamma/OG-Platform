/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class CornishFisherDeltaGammaVaRCalculator extends Function1D<SkewKurtosisStatistics<?>, Double> {
  private double _horizon;
  private double _periods;
  private double _z;
  private double _mult;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public CornishFisherDeltaGammaVaRCalculator(final double horizon, final double periods, final double quantile) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    _horizon = horizon;
    _periods = periods;
    _z = _normal.getInverseCDF(quantile);
    setMultiplier();
  }

  public void setHorizon(final double horizon) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    _horizon = horizon;
    setMultiplier();
  }

  public void setPeriods(final double periods) {
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    _periods = periods;
    setMultiplier();
  }

  public void setQuantile(final double quantile) {
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    _z = _normal.getInverseCDF(quantile);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt(_horizon / _periods);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final SkewKurtosisStatistics<?> statistics) {
    if (statistics == null)
      throw new IllegalArgumentException("Statistics were null");
    final double zSq = _z * _z;
    final double mean = statistics.getMean();
    final double std = statistics.getStandardDeviation();
    final double skew = statistics.getSkew();
    final double kurtosis = statistics.getKurtosis();
    final double x = _z + skew * (zSq - 1) / 6. + kurtosis * _z * (zSq - 3) / 24. - skew * skew * _z * (2 * zSq - 5) / 36.;
    return x * std * _mult + mean * _mult * _mult;
  }

}
