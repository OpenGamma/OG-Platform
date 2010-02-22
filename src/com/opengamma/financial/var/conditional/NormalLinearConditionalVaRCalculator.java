/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.conditional;

import com.opengamma.financial.var.NormalStatistics;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class NormalLinearConditionalVaRCalculator extends Function1D<NormalStatistics<?>, Double> {
  private double _horizon;
  private double _periods;
  private double _quantile;
  private double _mult;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public NormalLinearConditionalVaRCalculator(final double horizon, final double periods, final double quantile) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
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
    _quantile = quantile;
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = _normal.getPDF(_normal.getInverseCDF(_quantile)) * Math.sqrt(_horizon / _periods) / (1 - _quantile);
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    if (statistics == null)
      throw new IllegalArgumentException("Statistics were null");
    return _mult * statistics.getStandardDeviation();
  }
}
