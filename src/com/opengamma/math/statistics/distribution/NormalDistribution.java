/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Probability;

/**
 * 
 * @author emcleod
 */

public class NormalDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister64(new Date());
  private final double _mean;
  private final double _standardDeviation;
  private final Normal _normal;

  public NormalDistribution(final double mean, final double standardDeviation) {
    if (standardDeviation < 0)
      throw new IllegalArgumentException("Standard deviation cannot be less than zero");
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, _randomEngine);
  }

  public NormalDistribution(final double mean, final double standardDeviation, final RandomEngine randomEngine) {
    if (standardDeviation < 0)
      throw new IllegalArgumentException("Standard deviation cannot be less than zero");
    if (randomEngine == null)
      throw new IllegalArgumentException("Engine was null");
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  @Override
  public double getCDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _normal.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _normal.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  @Override
  public double getInverseCDF(final Double p) {
    if (p == null)
      throw new IllegalArgumentException("p was null");
    if (p > 1 || p < 0)
      throw new IllegalArgumentException("Probability must be >= 0 and <= 1");
    return Probability.normalInverse(p);
  }

  public double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
