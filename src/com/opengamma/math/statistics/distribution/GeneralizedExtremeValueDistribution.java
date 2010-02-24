/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.CompareUtils;

/**
 * @author emcleod
 * 
 */
public class GeneralizedExtremeValueDistribution implements ProbabilityDistribution<Double> {
  private final double _mu;
  private final double _sigma;
  private final double _ksi;
  private final boolean _ksiIsZero;

  public GeneralizedExtremeValueDistribution(final double mu, final double sigma, final double ksi) {
    _mu = mu;
    _sigma = sigma;
    _ksi = ksi;
    _ksiIsZero = CompareUtils.closeEquals(ksi, 0, 1e-13);
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
    return Math.exp(-getT(x));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#
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
    final double t = getT(x);
    return Math.pow(t, _ksi + 1) * Math.exp(-t) / _sigma;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * 
   * 
   * 
   * com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom
   * ()
   */
  @Override
  public double nextRandom() {
    throw new NotImplementedException();
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

  private double getT(final double x) {
    return _ksiIsZero ? Math.exp(-(x - _mu) / _sigma) : Math.pow(1 + _ksi * (x - _mu) / _sigma, -1. / _ksi);
  }

}
