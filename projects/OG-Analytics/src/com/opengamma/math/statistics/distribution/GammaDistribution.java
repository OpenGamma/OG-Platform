/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.ArgumentChecker;

/**
 * A Gamma distribution with shape parameter {@latex.inline $k$} and scale parameter {@latex.inline $\\theta$}.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the cdf, pdf
 * and {@latex.inline $\\Gamma$}-distributed random numbers.
 * 
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
    ArgumentChecker.notNegativeOrZero(k, "k");
    ArgumentChecker.notNegativeOrZero(theta, "theta");
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
    ArgumentChecker.notNegativeOrZero(k, "k");
    ArgumentChecker.notNegativeOrZero(theta, "theta");
    Validate.notNull(engine);
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
   * @param x {@latex.inline $x$}
   * @return The CDF for {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _gamma.cdf(x);
  }

  /**
   * The inverse cdf has not been implemented
   * 
   * @param p {@latex.inline $p$}
   * @return Nothing
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
   * @param x {@latex.inline $x$}
   * @return The PDF for {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
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
