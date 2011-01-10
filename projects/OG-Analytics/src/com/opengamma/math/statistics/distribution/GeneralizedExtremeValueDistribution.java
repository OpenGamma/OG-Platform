/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * 
 * The generalized extreme value distribution is a family of continuous probability distributions that combines the Gumbel (type I),
 * Frechet (type II) and Weibull (type III) families of distributions.
 * <p>
 * This distribution has location parameter {@latex.inline $\\mu$}, shape parameter {@latex.inline $\\xi$}
 * and scale parameter {@latex.inline $\\sigma$}, with
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * \\mu&\\in&\\Re,\\\\
 * \\xi&\\in&\\Re,\\\\
 * \\sigma&>&0
 * \\end{eqnarray*}}
 * and support
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * x\\in
 * \\begin{cases}
 * \\left[\\mu - \\frac{\\sigma}{\\xi}, +\\infty\\right) & \\text{when } \\xi > 0\\\\
 * (-\\infty,+\\infty) & \\text{when } \\xi = 0\\\\
 * \\left(-\\infty, \\mu - \\frac{\\sigma}{\\xi}\\right] & \\text{when } \\xi < 0
 * \\end{cases}
 * \\end{eqnarray*}}
 *  
 */
// TODO accent on Frechet
public class GeneralizedExtremeValueDistribution implements ProbabilityDistribution<Double> {
  private final double _mu;
  private final double _sigma;
  private final double _ksi;
  private final boolean _ksiIsZero;

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
   */
  public GeneralizedExtremeValueDistribution(final double mu, final double sigma, final double ksi) {
    ArgumentChecker.notNegative(sigma, "sigma");
    _mu = mu;
    _sigma = sigma;
    _ksi = ksi;
    _ksiIsZero = CompareUtils.closeEquals(ksi, 0, 1e-13);
  }

  /**
   * Returns the cdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{eqnarray*}
   * F(x) &=&e^{-t(x)}\\quad\\text{where}\\\\
   * t(x)&=&
   * \\begin{cases}
   * \\left(1 + \\xi\\frac{x-\\mu}{\\sigma}\\right)^{-\\frac{1}{\\xi}} & \\text{if } \\xi \\neq 0,\\\\
   * e^{-\\frac{x-\\mu}{\\sigma}} & \\text{if } \\xi = 0.
   * \\end{cases}
   * \\end{eqnarray*}}
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getCDF
   * @param x {@latex.inline $x$}
   * @return The CDF for {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   * @throws IllegalArgumentException
   *           If {@latex.inline $x \\not\\in$} support
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return Math.exp(-getT(x));
  }

  /**
   * 
   * This method is not implemented
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getInverseCDF
   * @param p {@latex.inline $p$}
   * @return Method is not implemented
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
   * f(x)&=&\\frac{t(x)^{\\xi + 1}e^{-t(x)}}{\\sigma}\\quad\\text{where}\\\\
   * t(x)&=&
   * \\begin{cases}
   * \\left(1 + \\xi\\frac{x-\\mu}{\\sigma}\\right)^{-\\frac{1}{\\xi}} & \\text{if } \\xi \\neq 0,\\\\
   * e^{-\\frac{x-\\mu}{\\sigma}} & \\text{if } \\xi = 0.
   * \\end{cases}
   * \\end{eqnarray*}}
   *  
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#getPDF
   * @param x {@latex.inline $x$}
   * @return The PDF for {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} was null
   * @throws IllegalArgumentException
   *           If {@latex.inline $x \\not\\in$} support
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    final double t = getT(x);
    return Math.pow(t, _ksi + 1) * Math.exp(-t) / _sigma;
  }

  /**
   * 
   * This method is not implemented.
   * 
   * @see com.opengamma.math.statistics.distribution.ProbabilityDistribution#nextRandom()
   * @return This method is not implemented
   * @throws NotImplementedException
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
    if (_ksiIsZero) {
      return Math.exp(-(x - _mu) / _sigma);
    }
    if (_ksi < 0 && x > _mu - _sigma / _ksi) {
      throw new IllegalArgumentException("Support for GEV is in the range -infinity -> mu - sigma / ksi when ksi < 0");
    }
    if (_ksi > 0 && x < _mu - _sigma / _ksi) {
      throw new IllegalArgumentException("Support for GEV is in the range mu - sigma / ksi -> +infinity when ksi > 0");
    }
    return Math.pow(1 + _ksi * (x - _mu) / _sigma, -1. / _ksi);
  }

}
