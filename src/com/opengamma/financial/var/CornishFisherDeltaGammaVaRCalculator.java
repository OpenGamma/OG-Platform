/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
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
