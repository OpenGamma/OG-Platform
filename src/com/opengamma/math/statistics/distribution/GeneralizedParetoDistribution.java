/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.CompareUtils;

/**
 * @author emcleod
 * 
 */
public class GeneralizedParetoDistribution implements ProbabilityDistribution<Double> {
  private final double _mu;
  private final double _sigma;
  private final double _ksi;
  // TODO better seed
  private RandomEngine _engine = new MersenneTwister64(new Date());

  public GeneralizedParetoDistribution(final double mu, final double sigma, final double ksi) {
    if (sigma < 0)
      throw new IllegalArgumentException("Sigma must be positive");
    if (CompareUtils.closeEquals(ksi, 0, 1e-15))
      throw new IllegalArgumentException("Ksi cannot be zero");
    _mu = mu;
    _sigma = sigma;
    _ksi = ksi;
  }

  public GeneralizedParetoDistribution(final double mu, final double sigma, final double ksi, final RandomEngine engine) {
    if (sigma < 0)
      throw new IllegalArgumentException("Sigma must be positive");
    if (CompareUtils.closeEquals(ksi, 0, 1e-15))
      throw new IllegalArgumentException("Ksi cannot be zero");
    if (engine == null)
      throw new IllegalArgumentException("Engine cannot be null");
    _mu = mu;
    _sigma = sigma;
    _ksi = ksi;
    _engine = engine;
  }

  public double getMu() {
    return _mu;
  }

  public double getSigma() {
    return _sigma;
  }

  public double getKsi() {
    return _ksi;
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
      throw new IllegalArgumentException("Value was null");
    return 1 - Math.pow(1 + _ksi * getZ(x), -1. / _ksi);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.statistics.distribution.ProbabilityDistribution#
   * getInverseCDF(java.lang.Double)
   */
  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
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
      throw new IllegalArgumentException("Value was null");
    return Math.pow(1 + _ksi * getZ(x), -(1. / _ksi + 1)) / _sigma;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * 
   * 
   * 
   * 
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom
   * ()
   */
  @Override
  public double nextRandom() {
    return _mu + _sigma * (Math.pow(_engine.nextDouble(), -_ksi) - 1) / _ksi;
  }

  private double getZ(final double x) {
    if (_ksi > 0 && x < _mu)
      throw new IllegalArgumentException("Support for GPD is in the range x >= mu if ksi > 0");
    if (_ksi < 0 && (x <= _mu || x >= _mu - _sigma / _ksi))
      throw new IllegalArgumentException("Support for GPD is in the range mu <= x <= mu - sigma / ksi if ksi < 0");
    return (x - _mu) / _sigma;
  }
}
