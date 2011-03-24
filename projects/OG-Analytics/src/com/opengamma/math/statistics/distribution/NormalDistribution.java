/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Probability;

/**
 * The normal distribution is a continuous probability distribution with probability density function
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * f(x) = \\frac{1}{\\sqrt{2\\pi}\\sigma} e^{-\\frac{(x - \\mu)^2}{2\\sigma^2}}
 * \\end{align*}
 * }
 * where {@latex.inline $\\mu$} is the mean and {@latex.inline $\\sigma$} the standard deviation of 
 * the distribution.
 * <p>
 * For values of the cumulative distribution function {@latex.inline $|x| > 7.6$} this class calculates the cdf 
 * directly. For all other methods and values of {@latex.inline $x$}, this class is a wrapper for the
 * <a href="http://acs.lbl.gov/software/colt/api/cern/jet/random/Normal.html">Colt</a> implementation of the normal distribution.
 */
public class NormalDistribution implements ProbabilityDistribution<Double> {
  private static final double XMIN = -7.6;
  private static final double DELTA = 0.05;

  // TODO need a better seed
  private final RandomEngine _randomEngine;
  private final double _mean;
  private final double _standardDeviation;
  private final Normal _normal;

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution
   */
  public NormalDistribution(final double mean, final double standardDeviation) {
    this(mean, standardDeviation, new MersenneTwister64(new Date()));
  }

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution
   * @param randomEngine A generator of random numbers
   */
  public NormalDistribution(final double mean, final double standardDeviation, final RandomEngine randomEngine) {
    Validate.isTrue(standardDeviation > 0, "standard deviation");
    Validate.notNull(randomEngine);
    _mean = mean;
    _standardDeviation = standardDeviation;
    _randomEngine = randomEngine;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  /**
   * {@inheritDoc}
   * The cern.jet.random library gives poor results for {@latex.inline $x < -8$}, and returns zero for {@latex.inline $x < -8.37$}, so beyond {@latex.inline $x < -7.6$} 
   * we use the approximation 
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{align*}
   * N(x) \\approx \\frac{N'(x)}{\\sqrt{1 + x^2}}
   * \\end{align*}
   * }
   * and use the symmetry of the distribution for {@latex.inline $x > 7.6$}  
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);

    if (x <= XMIN) {
      return _normal.pdf(x) / Math.sqrt(1 + x * x);
    }
    if (x < XMIN + DELTA) {
      // smooth the two approximations together
      double a = Math.sqrt(-Math.log(getCDF(XMIN)));
      double b = Math.sqrt(-Math.log(getCDF(XMIN + DELTA)));
      double temp = (a * (XMIN + DELTA - x) + b * (x - XMIN)) / DELTA;
      return Math.exp(-temp * temp);
    }
    if (x > -(XMIN + DELTA)) {
      return 1.0 - getCDF(-x);
    }
    return _normal.cdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _normal.pdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    Validate.isTrue(p >= 0 && p <= 1, "Probability must be >= 0 and <= 1");
    return Probability.normalInverse(p);
  }

  /**
   * @return The mean
   */
  public double getMean() {
    return _mean;
  }

  /**
   * @return The standard deviation
   */
  public double getStandardDeviation() {
    return _standardDeviation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mean);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _randomEngine.hashCode();
    temp = Double.doubleToLongBits(_standardDeviation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NormalDistribution other = (NormalDistribution) obj;
    if (Double.doubleToLongBits(_mean) != Double.doubleToLongBits(other._mean)) {
      return false;
    }
    if (Double.doubleToLongBits(_standardDeviation) != Double.doubleToLongBits(other._standardDeviation)) {
      return false;
    }
    return ObjectUtils.equals(_randomEngine, other._randomEngine);
  }

}
