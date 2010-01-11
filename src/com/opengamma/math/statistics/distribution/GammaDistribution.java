/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;

import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 * @author emcleod
 */
public class GammaDistribution implements ProbabilityDistribution<Double> {
  private final Gamma _gamma;

  public GammaDistribution(final double k, final double theta) {
    if (k <= 0)
      throw new IllegalArgumentException("k must be positive");
    if (theta <= 0)
      throw new IllegalArgumentException("Theta must be positive");
    // TODO better seed
    _gamma = new Gamma(k, theta, new MersenneTwister(1));
  }

  public GammaDistribution(final double alpha, final double lambda, final RandomEngine engine) {
    if (alpha <= 0)
      throw new IllegalArgumentException("k must be positive");
    if (lambda <= 0)
      throw new IllegalArgumentException("Theta must be positive");
    if (engine == null)
      throw new IllegalArgumentException("Random engine was null");
    _gamma = new Gamma(alpha, lambda, engine);
  }

  @Override
  public double getCDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _gamma.cdf(x);
  }

  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  @Override
  public double getPDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _gamma.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _gamma.nextDouble();
  }

}
