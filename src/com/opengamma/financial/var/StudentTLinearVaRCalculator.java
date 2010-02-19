/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * @author emcleod
 * 
 */
public class StudentTLinearVaRCalculator extends Function1D<NormalStatistics<?>, Double> {
  private double _horizon;
  private double _periods;
  private double _quantile;
  private double _dof;
  private double _mult;
  private ProbabilityDistribution<Double> _studentT;

  public StudentTLinearVaRCalculator(final double horizon, final double periods, final double quantile, final double dof) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    if (dof <= 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
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

  public void setDegreesOfFreedom(final double dof) {
    if (dof <= 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt((_dof - 2) * _horizon / _dof / _periods) * _studentT.getInverseCDF(_quantile);
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    if (statistics == null)
      throw new IllegalArgumentException("Statistics were null");
    return _mult * statistics.getStandardDeviation() - statistics.getMean();
  }
}
