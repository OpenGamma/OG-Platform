/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import cern.jet.random.StudentT;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.InverseIncompleteBetaFunction;

/**
 * 
 * @author emcleod
 */
public class StudentTDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister(1);
  private final double _degFreedom;
  private final StudentT _dist;
  private final Function1D<Double, Double> _beta;

  public StudentTDistribution(final double degFreedom) {
    if (degFreedom <= 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than zero");
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, _randomEngine);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  public StudentTDistribution(final double degFreedom, final RandomEngine engine) {
    if (degFreedom < 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than zero");
    if (engine == null)
      throw new IllegalArgumentException("Engine was null");
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, engine);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  @Override
  public double getCDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _dist.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _dist.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _dist.nextDouble();
  }

  @Override
  public double getInverseCDF(final Double p) {
    if (p == null)
      throw new IllegalArgumentException("Probability was null");
    if (p < 0 || p > 1)
      throw new IllegalArgumentException("Probability must be between 0 and 1");
    final double x = _beta.evaluate(2 * Math.min(p, 1 - p));
    return Math.signum(p - 0.5) * Math.sqrt(_degFreedom * (1. / x - 1));
  }

  public double getDegreesOfFreedom() {
    return _degFreedom;
  }
}
