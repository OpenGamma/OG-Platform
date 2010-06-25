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
public class NormalLinearVaRCalculator extends VaRCalculator<NormalStatistics<?>> {
  private double _mult;
  private double _z;
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  public NormalLinearVaRCalculator(final double horizon, final double periods, final double quantile) {
    super(horizon, periods, quantile);
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

  private void setMultiplier() {
    _mult = Math.sqrt(getHorizon() / getPeriods());
    _z = _normal.getInverseCDF(getQuantile());
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    Validate.notNull(statistics, "statistics");
    return _z * _mult * statistics.getStandardDeviation() - _mult * _mult * statistics.getMean();
  }

}
