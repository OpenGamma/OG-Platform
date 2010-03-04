/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * @author emcleod
 * 
 */
public class LaplaceDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private RandomEngine _engine = new MersenneTwister64(new Date());
  private final double _mu;
  private final double _b;

  public LaplaceDistribution(final double mu, final double b) {
    if (b <= 0)
      throw new IllegalArgumentException("B must be greater than zero");
    _mu = mu;
    _b = b;
  }

  public LaplaceDistribution(final double mu, final double b, final RandomEngine engine) {
    if (b <= 0)
      throw new IllegalArgumentException("B must be greater than zero");
    if (engine == null)
      throw new IllegalArgumentException("Engine was null");
    _mu = mu;
    _b = b;
    _engine = engine;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#getCDF
   * (java.lang.Object)
   */
  @Override
  public double getCDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return 0.5 * (1 + Math.signum(x - _mu) * (1 - Math.exp(-Math.abs(x - _mu) / _b)));
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.statistics.distribution.ProbabilityDistribution#
   * getInverseCDF(java.lang.Double)
   */
  @Override
  public double getInverseCDF(final Double p) {
    if (p == null)
      throw new IllegalArgumentException("p was null");
    if (p > 1 || p < 0)
      throw new IllegalArgumentException("Probability must be >= 0 and <= 1");
    return _mu - _b * Math.signum(p - 0.5) * Math.log(1 - 2 * Math.abs(p - 0.5));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#getPDF
   * (java.lang.Object)
   */
  @Override
  public double getPDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return Math.exp(-Math.abs(x - _mu) / _b) / (2 * _b);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom
   * ()
   */
  @Override
  public double nextRandom() {
    final double u = _engine.nextDouble() - 0.5;
    return _mu - _b * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
  }
}
