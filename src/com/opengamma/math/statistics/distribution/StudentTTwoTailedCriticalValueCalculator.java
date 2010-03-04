/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class StudentTTwoTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final Function1D<Double, Double> _calc;

  public StudentTTwoTailedCriticalValueCalculator(final double nu) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    _calc = new StudentTOneTailedCriticalValueCalculator(nu);
  }

  public StudentTTwoTailedCriticalValueCalculator(final double nu, final RandomEngine engine) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    if (engine == null)
      throw new IllegalArgumentException("Engine was null");
    _calc = new StudentTOneTailedCriticalValueCalculator(nu, engine);
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
    return _calc.evaluate(0.5 + 0.5 * x);
  }
}
