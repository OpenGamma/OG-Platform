/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.IncompleteBetaFunction;
import com.opengamma.math.function.special.InverseIncompleteBetaFunction;

/**
 * 
 * @author emcleod
 */
public class TwoTailedStudentTDistribution implements ProbabilityDistribution<Double> {
  private final double _degreesOfFreedom;
  private final Function1D<Double, Double> _beta;

  public TwoTailedStudentTDistribution(final double degreesOfFreedom) {
    if (degreesOfFreedom < 0)
      throw new IllegalArgumentException("Degrees of freedom must be positive");
    _degreesOfFreedom = degreesOfFreedom;
    _beta = new IncompleteBetaFunction(degreesOfFreedom * 0.5, 0.5);
  }

  @Override
  public double getCDF(final Double x) {
    if (x < 0)
      throw new IllegalArgumentException("x must be positive");
    return 1 - _beta.evaluate(_degreesOfFreedom / (_degreesOfFreedom + x * x));
  }

  @Override
  public double getInverseCDF(final Double p) {
    if (p < 0 || p > 1)
      throw new IllegalArgumentException("p must lie in the range 0 to 1");
    final Function1D<Double, Double> betaInverse = new InverseIncompleteBetaFunction(p, 0.5 * _degreesOfFreedom);
    final double x = betaInverse.evaluate(0.5);
    return Math.sqrt(_degreesOfFreedom * (1 - x) / x);
  }

  @Override
  public double getPDF(final Double x) {
    throw new NotImplementedException();
  }

  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }
}
