/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.GammaFunction;

/**
 * @author emcleod
 * 
 */
public class StudentTOneTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final double _nu;
  private final double _a;

  public StudentTOneTailedCriticalValueCalculator(final double nu) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    _nu = nu;
    final double halfNu = 0.5 * nu;
    final Function1D<Double, Double> gamma = new GammaFunction();
    _a = Math.sqrt(nu * Math.PI) * gamma.evaluate(halfNu) / gamma.evaluate(halfNu + 0.5);
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
    return Math.sqrt(_nu * (Math.pow(x * _a, -2. / (_nu + 1)) - 1));
  }

}
