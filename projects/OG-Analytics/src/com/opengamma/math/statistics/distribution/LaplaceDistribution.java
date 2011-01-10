/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LaplaceDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private RandomEngine _engine = new MersenneTwister64(new Date());
  private final double _mu;
  private final double _b;

  /**
   * 
   * @param mu 
   *          The location parameter
   * @param b
   *          The scale parameter
   * @throws IllegalArgumentException
   *           If {@latex.inline $b < 0$}
   */
  public LaplaceDistribution(final double mu, final double b) {
    ArgumentChecker.notNegativeOrZero(b, "b");
    _mu = mu;
    _b = b;
  }

  /**
   * 
   * @param mu 
   *          The location parameter
   * @param b
   *          The scale parameter
   * @param engine
   *          A <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">RandomEngine</a>, a uniform random number
   *          generator
   * @throws IllegalArgumentException
   *           If {@latex.inline $b < 0$}
   * @throws IllegalArgumentException
   *           If the engine is null
   */
  public LaplaceDistribution(final double mu, final double b, final RandomEngine engine) {
    ArgumentChecker.notNegativeOrZero(b, "b");
    Validate.notNull(engine);
    _mu = mu;
    _b = b;
    _engine = engine;
  }

  /**
   * 
   * The cdf is given by:
   * <p>
   * {@latex.inline %preamble{\\usepackage{amsmath}}
   * \\begin{eqnarray*}
   * F(x)&=&
   * \\begin{cases}
   * \\frac{1}{2}e^{\\frac{x-\\mu}{b}} & \\text{if } x < \\mu\\\\
   * 1-\\frac{1}{2}e^{-\\frac{x-\\mu}{b}} & \\text{if } x\\geq \\mu
   * \\end{cases}
   * \\end{eqnarray*}}
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getCDF
   * @param x {@latex.inline $x$}
   * @return The CDF of {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} is null
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return 0.5 * (1 + Math.signum(x - _mu) * (1 - Math.exp(-Math.abs(x - _mu) / _b)));
  }

  /**
   * 
   * The inverse cdf is given by:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * F^{-1}(p)=\\mu-b\\text{ sgn}(p-0.5)\\ln(1-2|p-0.5|)
   * \\end{equation*}}
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getInverseCDF
   * @param p {@latex.inline $p$}
   * @return The inverse CDF of {@latex.inline $p$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $p$} is null
   * @throws IllegalArgumentException
   *           If {@latex.inline $p > 1$} or {@latex.inline $p < 0$}
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    if (!ArgumentChecker.isInRangeInclusive(0, 1, p)) {
      throw new IllegalArgumentException("Probability must lie between 0 and 1: have " + p);
    }
    return _mu - _b * Math.signum(p - 0.5) * Math.log(1 - 2 * Math.abs(p - 0.5));
  }

  /**
   * 
   * The pdf is given by:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * f(x|\\mu, b)=\\frac{1}{2b}e^{-\\frac{|x-\\mu|}{b}}
   * \\end{equation*}}
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getPDF
   * @param x {@latex.inline $x$}
   * @return The PDF of {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} is null
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return Math.exp(-Math.abs(x - _mu) / _b) / (2 * _b);
  }

  /**
   * 
   * Given a uniform random variable {@latex.inline $U$} drawn from the interval {@latex.inline $(-\\frac{1}{2}, \\frac{1}{2}]$},  
   * a Laplace-distributed random variable with parameters {@latex.inline $\\mu$} and {@latex.inline $b$} is given by:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * X=\\mu-b\\text{ sgn}(U)\\ln(1-2|U|)
   * \\end{equation*}}
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom
   * @return The next random number from this distribution
   * 
   */
  @Override
  public double nextRandom() {
    final double u = _engine.nextDouble() - 0.5;
    return _mu - _b * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
  }

  public double getMu() {
    return _mu;
  }

  public double getB() {
    return _b;
  }
}
