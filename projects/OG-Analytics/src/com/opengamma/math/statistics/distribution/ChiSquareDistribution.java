/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.ChiSquare;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function2D;
import com.opengamma.math.function.special.InverseIncompleteGammaFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * A {@latex.inline $\\chi^2$} distribution with {@latex.inline $k$} degrees of freedom is the distribution of the sum of squares
 * of {@latex.inline $k$} independent standard normal random variables.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the
 * cdf, pdf and {@latex.inline $\\chi^2$}-distributed random numbers.
 * 
 */
public class ChiSquareDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _engine = new MersenneTwister64(new Date());
  private final Function2D<Double, Double> _inverseFunction = new InverseIncompleteGammaFunction();
  private final ChiSquare _chiSquare;
  private final double _degrees;

  /**
   * 
   * @param degrees
   *          The degrees of freedom of the distribution
   * @throws IllegalArgumentException
   *          If the degrees of freedom is less than one
   */
  public ChiSquareDistribution(final double degrees) {
    if (degrees < 1) {
      throw new IllegalArgumentException("Degrees of freedom must be greater than or equal to one");
    }
    _chiSquare = new ChiSquare(degrees, _engine);
    _degrees = degrees;
  }

  /**
   * 
   * @param degrees
   *          The degrees of freedom of the distribution
   * @param engine
   *          A <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">RandomEngine</a>, a uniform random number
   *          generator
   * @throws IllegalArgumentException
   *           If the degrees of freedom is less than one
   * @throws IllegalArgumentException
   *           If the random number generator was null
   */
  public ChiSquareDistribution(final double degrees, final RandomEngine engine) {
    if (degrees < 1) {
      throw new IllegalArgumentException("Degrees of freedom must be greater than or equal to one");
    }
    Validate.notNull(engine);
    _chiSquare = new ChiSquare(degrees, engine);
    _degrees = degrees;
  }

  /**
   * 
   * Returns the cdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * F(x; k)=\\frac{\\gamma\\left(\\frac{k}{2}, \\frac{x}{2}\\right)}{\\Gamma\\left(\\frac{k}{2}\\right)}
   * \\end{equation*}}
   * where {@latex.inline $\\gamma(y, z)$} is the lower incomplete Gamma function and {@latex.inline $\\Gamma(y)$} is the Gamma function.
   * 
   * @param x {@latex.inline $x$}
   * @return Value of the CDF at {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} is null
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);
    return _chiSquare.cdf(x);
  }

  /**
   * 
   * Returns the pdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * f(x; k)=\\frac{x^{\\frac{k}{2}-1}e^{-\\frac{x}{2}}}{2^{\\frac{k}{2}}\\Gamma\\left(\\frac{k}{2}\\right)}
   * \\end{equation*}}
   * where {@latex.inline $\\Gamma(y)$} is the Gamma function.
   * 
   * @param x {@latex.inline $x$}
   * @return Value of the PDF at {@latex.inline $x$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $x$} is null
   * 
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _chiSquare.pdf(x);
  }

  /**
   * 
   * Returns the inverse cdf:
   * <p>
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * F^{-1}(x; k) = 2\\gamma^{-1}\\left(\\frac{k}{2}, p\\right)
   * \\end{equation*}}
   * where {@latex.inline $\\gamma^{-1}(y)$} is the inverse incomplete Gamma function.
   * 
   * @see com.opengamma.math.function.special.InverseIncompleteGammaFunction
   * @param p {@latex.inline $p$}
   * @return Value of the inverse CDF at {@latex.inline $p$}
   * @throws IllegalArgumentException
   *           If {@latex.inline $p$} is null
   * @throws IllegalArgumentException
   *           If {@latex.inline $p < 0$ or $p \\geq 1$}
   * 
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, p)) {
      throw new IllegalArgumentException("Probability must lie between 0 and 1: have " + p);
    }
    return 2 * _inverseFunction.evaluate(0.5 * _degrees, p);
  }

  @Override
  public double nextRandom() {
    return _chiSquare.nextDouble();
  }

  public double getDegreesOfFreedom() {
    return _degrees;
  }
}
