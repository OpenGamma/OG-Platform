/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.StudentT;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.InverseIncompleteBetaFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class StudentTDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _randomEngine = new MersenneTwister64(new Date());
  private final double _degFreedom;
  private final StudentT _dist;
  private final Function1D<Double, Double> _beta;

  public StudentTDistribution(final double degFreedom) {
    ArgumentChecker.notNegativeOrZero(degFreedom, "degrees of freedom");
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, _randomEngine);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  public StudentTDistribution(final double degFreedom, final RandomEngine engine) {
    ArgumentChecker.notNegativeOrZero(degFreedom, "degrees of freedom");
    Validate.notNull(engine);
    _degFreedom = degFreedom;
    _dist = new StudentT(degFreedom, engine);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _dist.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _dist.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _dist.nextDouble();
  }

  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    if (!ArgumentChecker.isInRangeExclusive(0, 1, p)) {
      throw new IllegalArgumentException("Probability must be between 0 and 1");
    }
    final double x = _beta.evaluate(2 * Math.min(p, 1 - p));
    return Math.signum(p - 0.5) * Math.sqrt(_degFreedom * (1. / x - 1));
  }

  public double getDegreesOfFreedom() {
    return _degFreedom;
  }
}
