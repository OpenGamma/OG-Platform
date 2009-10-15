/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Probability;

/**
 * 
 * @author emcleod
 */

public class NormalProbabilityDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister(0);
  private final double _mean;
  private final double _standardDeviation;
  private final Normal _normal;

  public NormalProbabilityDistribution(final double mean, final double standardDeviation) {
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, _randomEngine);
  }

  public NormalProbabilityDistribution(final double mean, final double standardDeviation, final RandomEngine randomEngine) {
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  @Override
  public double getCDF(final Double x) {
    return _normal.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    return _normal.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  @Override
  public double getInverseCDF(final Double p) {
    return Probability.normalInverse(p);
  }

  public double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
