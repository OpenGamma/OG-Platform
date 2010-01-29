/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class StudentTTwoTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final Function1D<Double, Double> _inversePDF;

  public StudentTTwoTailedCriticalValueCalculator(final double nu) {
    if (nu < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    _inversePDF = new StudentTOneTailedCriticalValueCalculator(nu);
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
    return 2 * _inversePDF.evaluate(x);
  }
}
