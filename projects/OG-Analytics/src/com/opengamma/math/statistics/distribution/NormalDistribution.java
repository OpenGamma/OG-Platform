/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  private static final double XMIN = -7.6;
  private static final double DELTA = 0.05;

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

  /**
   *The cern.jet.random library gives poor results for x < -8, and returns zero for x < -8.37, so beyond x < -7.6 we used the approximation  N(x) = N'(x)/sqrt(1+x^2) and use the
   *symmetry for x > 7.6  
   *@param x The value for which to find the cdf
   *@return The cdf
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);

    if (x <= XMIN) {
      return _normal.pdf(x) / Math.sqrt(1 + x * x);
    }
    if (x < XMIN + DELTA) {
      // smooth the two approximations together
      double a = Math.sqrt(-Math.log(getCDF(XMIN)));
      double b = Math.sqrt(-Math.log(getCDF(XMIN + DELTA)));
      double temp = (a * (XMIN + DELTA - x) + b * (x - XMIN)) / DELTA;
      return Math.exp(-temp * temp);
    }
    if (x > -(XMIN + DELTA)) {
      return 1.0 - getCDF(-x);
    }
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
