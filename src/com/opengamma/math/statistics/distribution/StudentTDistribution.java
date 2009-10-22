/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;

import cern.jet.random.StudentT;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 * @author emcleod
 */
public class StudentTDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister(0);
  private final double _degFreedom;
  private final StudentT _dist;

  public StudentTDistribution(final double degFreedom) {
    if (degFreedom < 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than zero");
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, _randomEngine);
  }

  public StudentTDistribution(final double degFreedom, final RandomEngine engine) {
    if (degFreedom < 0)
      throw new IllegalArgumentException("Degrees of freedom must be greater than zero");
    if (engine == null)
      throw new IllegalArgumentException("Engine was null");
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, engine);
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
    throw new NotImplementedException();
  }

  public double getDegreesOfFreedom() {
    return _degFreedom;
  }
}
