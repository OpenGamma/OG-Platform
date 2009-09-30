/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

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
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, _randomEngine);
  }

  @Override
  public double getCDF(final Double x) {
    return _dist.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    return _dist.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _dist.nextDouble();
  }

  public double getDegreesOfFreedom() {
    return _degFreedom;
  }
}
