/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.apache.commons.math.special.Gamma;

import com.opengamma.analytics.math.MathException;

/**
 * The non-central chi-squared distribution is a continuous probability
 * distribution with probability density function
 * $$
 * \begin{align*}
 * f_r(x) = \frac{e^-\frac{x + \lambda}{2}x^{\frac{r}{2} - 1}}{2^{\frac{r}{2}}}\sum_{k=0}^\infty \frac{(\lambda k)^k}{2^{2k}k!\Gamma(k + \frac{r}{2})}
 * \end{align*}
 * $$
 * where $r$ is the number of degrees of freedom, $\lambda$ is the
 * non-centrality parameter and $\Gamma$ is the Gamma function ({@link
 * com.opengamma.analytics.math.function.special.GammaFunction}).
 * <p>
 * For the case where $r + \lambda > 2000$, the implementation of the cdf is taken from "An Approximation for the Noncentral Chi-Squared Distribution", Fraser et al.
 * (<a href="http://fisher.utstat.toronto.edu/dfraser/documents/192.pdf">link</a>). Otherwise, the algorithm is taken from "Computing the Non-Central Chi-Squared Distribution Function", Ding.
 */
public class NonCentralChiSquaredDistribution implements ProbabilityDistribution<Double> {
  private final double _lambdaOverTwo;
  private final int _k;
  private final double _dofOverTwo;
  private final double _pStart;
  private final double _eps = 1e-16;

  /**
   * @param degrees The number of degrees of freedom, not negative or zero
   * @param nonCentrality The non-centrality parameter, not negative
   */
  public NonCentralChiSquaredDistribution(final double degrees, final double nonCentrality) {
    Validate.isTrue(degrees > 0, "degrees of freedom must be > 0, have " + degrees);
    Validate.isTrue(nonCentrality >= 0, "non-centrality must be >= 0, have " + nonCentrality);
    _dofOverTwo = degrees / 2.0;
    _lambdaOverTwo = nonCentrality / 2.0;
    _k = (int) Math.round(_lambdaOverTwo);

    if (_lambdaOverTwo == 0) {
      _pStart = 0.0;
    } else {
      final double logP = -_lambdaOverTwo + _k * Math.log(_lambdaOverTwo) - Gamma.logGamma(_k + 1);
      _pStart = Math.exp(logP);
    }
  }

  private double getFraserApproxCDF(final double x) {
    final double s = Math.sqrt(_lambdaOverTwo * 2.0);
    final double mu = Math.sqrt(x);
    double z;
    if (Double.doubleToLongBits(mu) == Double.doubleToLongBits(s)) {
      z = (1 - _dofOverTwo * 2.0) / 2 / s;
    } else {
      z = mu - s - (_dofOverTwo * 2.0 - 1) / 2 * (Math.log(mu) - Math.log(s)) / (mu - s);
    }
    return (new NormalDistribution(0, 1)).getCDF(z);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x, "x");
    if (x < 0) {
      return 0.0;
    }

    if ((_dofOverTwo + _lambdaOverTwo) > 1000) {
      return getFraserApproxCDF(x);
    }

    double regGammaStart = 0;
    final double halfX = x / 2.0;
    final double logX = Math.log(halfX);
    try {
      regGammaStart = Gamma.regularizedGammaP(_dofOverTwo + _k, halfX);
    } catch (final org.apache.commons.math.MathException ex) {
      throw new MathException(ex);
    }

    double sum = _pStart * regGammaStart;
    double oldSum = Double.NEGATIVE_INFINITY;
    double p = _pStart;
    double regGamma = regGammaStart;
    double temp;
    int i = _k;

    // first add terms below _k
    while (i > 0 && Math.abs(sum - oldSum) / sum > _eps) {
      i--;
      p *= (i + 1) / _lambdaOverTwo;
      temp = (_dofOverTwo + i) * logX - halfX - Gamma.logGamma(_dofOverTwo + i + 1);
      regGamma += Math.exp(temp);
      oldSum = sum;
      sum += p * regGamma;
    }

    p = _pStart;
    regGamma = regGammaStart;
    oldSum = Double.NEGATIVE_INFINITY;
    i = _k;
    while (Math.abs(sum - oldSum) / sum > _eps) {
      i++;
      p *= _lambdaOverTwo / i;
      temp = (_dofOverTwo + i - 1) * logX - halfX - Gamma.logGamma(_dofOverTwo + i);
      regGamma -= Math.exp(temp);
      oldSum = sum;
      sum += p * regGamma;
    }

    return sum;
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double getPDF(final Double x) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }

  /**
   * @return The number of degrees of freedom
   */
  public double getDegrees() {
    return _dofOverTwo * 2.0;
  }

  /**
   * @return The non-centrality parameter
   */
  public double getNonCentrality() {
    return _lambdaOverTwo * 2.0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_dofOverTwo);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambdaOverTwo);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NonCentralChiSquaredDistribution other = (NonCentralChiSquaredDistribution) obj;
    if (Double.doubleToLongBits(_dofOverTwo) != Double.doubleToLongBits(other._dofOverTwo)) {
      return false;
    }
    return Double.doubleToLongBits(_lambdaOverTwo) == Double.doubleToLongBits(other._lambdaOverTwo);
  }
}
