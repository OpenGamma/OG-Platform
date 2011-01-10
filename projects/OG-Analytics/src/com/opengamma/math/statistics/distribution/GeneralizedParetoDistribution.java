/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 * The generalized Pareto distribution is a family of power law probability distributions with location parameter {@latex.inline $\\mu$}, shape parameter {@latex.inline $\\xi$}
 * and scale parameter {@latex.inline $\\sigma$}, where
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * \\mu&\\in&\\Re,\\\\
 * \\xi&\\in&\\Re,\\\\
 * \\sigma&>&0
 * \\end{eqnarray*}}
 * and with support
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * x\\geq\\mu\\quad\\quad\\quad(\\xi\\geq 0)\\\\
 * \\mu\\leq x\\leq\\mu-\\frac{\\sigma}{\\xi}\\quad(\\xi<0)
 * \\end{eqnarray*}}
 * 
 */
public class GeneralizedParetoDistribution implements ProbabilityDistribution<Double> {
  // TODO check cdf, pdf for support
  private final double _mu;
  private final double _sigma;
  private final double _ksi;
  // TODO better seed
  private RandomEngine _engine = new MersenneTwister64(new Date());

  /**
   * 
   * @param mu
   *          The location parameter
   * @param sigma
   *          The scale parameter
   * @param ksi
   *          The shape parameter
   * @throws IllegalArgumentException
   *           If {@latex.inline $\\sigma < 0$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $\\xi = 0$}
   */
  public GeneralizedParetoDistribution(final double mu, final double sigma, final double ksi) {
    ArgumentChecker.notNegative(sigma, "sigma");
    ArgumentChecker.notZero(ksi, 1e-15, "ksi");
    _mu = mu;
    _sigma = sigma;
    _ksi = ksi;
  }

  /**
   * 
   * @param mu
   *          The location parameter
   * @param sigma
   *          The scale parameter
   * @param ksi
   *          The shape parameter
   * @param engine
   *          A <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">RandomEngine</a>, a uniform random number
   *          generator
   * @throws IllegalArgumentException
   *           If {@latex.inline $\\sigma < 0$}
   * @throws IllegalArgumentException
   *           If the random number generator was null
   */
  public GeneralizedParetoDistribution(final double mu, final double sigma, final double ksi, final RandomEngine engine) {
    ArgumentChecker.notNegative(sigma, "sigma");
    ArgumentChecker.notNegativeOrZero(ksi, "ksi");
    Validate.notNull(engine);
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

  /**
   *
   * Returns the cdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{eqnarray*}
   * F(z, \\mu, \\sigma, \\xi)&=&1-\\left(1 + \\xi z\\right)^{-\\frac{1}{\\xi}}\\quad\\text{where}\\\\
   * z&=&\\frac{x-\\mu}{\\sigma}
   * \\end{eqnarray*}}
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getCDF
   * @param x {@latex.inline $x$}
   * @return The CDF of {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return 1 - Math.pow(1 + _ksi * getZ(x), -1. / _ksi);
  }

  /**
   * 
   * This method is not implemented.
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getInverseCDF
   * @param p {@latex.inline $p$}
   * @return This method is not implemented
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
  * {@latex.ilb %preamble{\\usepackage{amsmath}}
  * \\begin{eqnarray*}
  * f(z, \\mu, \\sigma, \\xi)&=&\\frac{\\left(1+\\xi z\\right)^{-\\left(\\frac{1}{\\xi} + 1\\right)}}{\\sigma}\\quad\\text{where}\\\\
  * z&=&\\frac{x-\\mu}{\\sigma}
  * \\end{eqnarray*}}
  * 
  * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getPDF
  * @param x {@latex.inline $x$}
  * @return The PDF of {@latex.inline $x$}
  * @throws IllegalArgumentException
  *           If {@latex.inline $x$} was null
  */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return Math.pow(1 + _ksi * getZ(x), -(1. / _ksi + 1)) / _sigma;
  }

  /**
   * 
   * If {@latex.inline $U$} is uniformly distributed on {@latex.inline $(0,1]$} then
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * X=\\mu + \\frac{\\sigma\\left(U^{-\\xi}-1\\right)}{\\xi}\\sim GPD(\\mu,\\sigma,\\xi)
   * \\end{equation*}}
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom
   * @return The next random number drawn from the distribution
   */
  @Override
  public double nextRandom() {
    return _mu + _sigma * (Math.pow(_engine.nextDouble(), -_ksi) - 1) / _ksi;
  }

  private double getZ(final double x) {
    if (_ksi > 0 && x < _mu) {
      throw new IllegalArgumentException("Support for GPD is in the range x >= mu if ksi > 0");
    }
    if (_ksi < 0 && (x <= _mu || x >= _mu - _sigma / _ksi)) {
      throw new IllegalArgumentException("Support for GPD is in the range mu <= x <= mu - sigma / ksi if ksi < 0");
    }
    return (x - _mu) / _sigma;
  }
}
