/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * @author emcleod
 * 
 */
public class StudentTLinearVaRCalculator extends VaRCalculator<NormalStatistics<?>> {
  private double _dof;
  private double _mult;
  private ProbabilityDistribution<Double> _studentT;

  public StudentTLinearVaRCalculator(final double horizon, final double periods, final double quantile, final double dof) {
    super(horizon, periods, quantile);
    if (dof <= 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
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

  public void setDegreesOfFreedom(final double dof) {
    if (dof <= 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt((_dof - 2) * getHorizon() / _dof / getPeriods()) * _studentT.getInverseCDF(getQuantile());
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    if (statistics == null)
      throw new IllegalArgumentException("Statistics were null");
    return _mult * statistics.getStandardDeviation() - statistics.getMean();
  }
}
