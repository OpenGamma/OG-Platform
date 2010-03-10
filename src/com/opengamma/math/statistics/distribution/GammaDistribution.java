/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;

import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * A Gamma distribution with shape parameter {@latex.inline $k$} and scale parameter {@latex.inline $\\theta$}.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the cdf, pdf
 * and {@latex.inline $\\Gamma$}-distributed random numbers.
 * 
 * @author emcleod
 */
public class GammaDistribution implements ProbabilityDistribution<Double> {
  private final Gamma _gamma;
  private final double _k;
  private final double _theta;

  /**
   * 
   * @param k 
   *          The shape parameter of the distribution
   * @param theta
   *          The scale parameter of the distribution
   * @throws IllegalArgumentException
   *           If {@latex.inline $k \\leq 0$} 
   * @throws IllegalArgumentException
   *           If {@latex.inline $\\theta \\leq 0$}
   */
  public GammaDistribution(final double k, final double theta) {
    if (k <= 0)
      throw new IllegalArgumentException("k must be positive");
    if (theta <= 0)
      throw new IllegalArgumentException("Theta must be positive");
    // TODO better seed
    _gamma = new Gamma(k, 1. / theta, new MersenneTwister(new Date()));
    _k = k;
    _theta = theta;
  }

  /**
   * 
   * @param k 
   *          The shape parameter of the distribution
   * @param theta
   *          The scale parameter of the distribution
   * @param engine
   *          A <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">RandomEngine</a>, a uniform random number
   *          generator
   * @throws IllegalArgumentException
   *           If {@latex.inline $k \\leq 0$} 
   * @throws IllegalArgumentException
   *           If {@latex.inline $\\theta \\leq 0$}
   * @throws IllegalArgumentException
   *           If the random number generator was null
   */
  public GammaDistribution(final double k, final double theta, final RandomEngine engine) {
    if (k <= 0)
      throw new IllegalArgumentException("k must be positive");
    if (theta <= 0)
      throw new IllegalArgumentException("Theta must be positive");
    if (engine == null)
      throw new IllegalArgumentException("Random engine was null");
    _gamma = new Gamma(k, 1. / theta, engine);
    _k = k;
    _theta = theta;
  }

  /**
   * 
   * Returns the cdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * F(x; k; \\theta)=\\frac{\\gamma\\left(k, \\frac{x}{\\theta}\\right)}{\\Gamma(k)}
   * \\end{equation*}}
   * 
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   */
  @Override
  public double getCDF(final Double x) {
    if (x == null)
      throw new IllegalArgumentException("x was null");
    return _gamma.cdf(x);
  }

  /**
   * The inverse cdf has not been implemented
   * 
   * @throws NotImplementedException
   */
  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  /**
   * 
   * Returns the pdf:
   * <p>
   * {@latex.inline %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * f(x; k; \\theta)=\\frac{x^{k-1}e^{-\\frac{x}{\\theta}}}{\\Gamma{k}\\theta^k}
   * \\end{equation*}}
   * 
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   */
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

  public double getK() {
    return _k;
  }

  public double getTheta() {
    return _theta;
  }
}
