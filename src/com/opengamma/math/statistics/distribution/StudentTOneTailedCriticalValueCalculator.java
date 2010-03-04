/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * @author emcleod
 * 
 */
public class StudentTOneTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final ProbabilityDistribution<Double> _dist;

  public StudentTOneTailedCriticalValueCalculator(final double nu) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    _dist = new StudentTDistribution(nu);
  }

  public StudentTOneTailedCriticalValueCalculator(final double nu, final RandomEngine engine) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    if (engine == null)
      throw new IllegalArgumentException("Engine was null");
    _dist = new StudentTDistribution(nu, engine);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    if (x < 0)
      throw new IllegalArgumentException("x must be positive");
    if (CompareUtils.closeEquals(x, 0.5, 1e-14))
      return 0.5;
    return _dist.getInverseCDF(x);
  }
}
