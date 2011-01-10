/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Probability;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NormalDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister64(new Date());
  private final double _mean;
  private final double _standardDeviation;
  private final Normal _normal;

  public NormalDistribution(final double mean, final double standardDeviation) {
    ArgumentChecker.notNegativeOrZero(standardDeviation, "standard deviation");
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, _randomEngine);
  }

  public NormalDistribution(final double mean, final double standardDeviation, final RandomEngine randomEngine) {
    ArgumentChecker.notNegativeOrZero(standardDeviation, "standard deviation");
    Validate.notNull(randomEngine);
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _normal.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _normal.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    if (!ArgumentChecker.isInRangeExclusive(0, 1, p)) {
      throw new IllegalArgumentException("Probability must be >= 0 and <= 1");
    }
    return Probability.normalInverse(p);
  }

  public double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
