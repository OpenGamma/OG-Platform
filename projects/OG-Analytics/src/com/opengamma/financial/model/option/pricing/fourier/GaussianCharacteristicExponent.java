/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.square;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * This class represents the characteristic exponent for a Brownian motion driven by normally-distributed increments
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi_X &= E\\left[e^{iuX}\\right]\\\\
 * &= \\frac{1}{\\sqrt{2\\pi}\\sigma}\\int_{-\\infty}^{\\infty} e^{iux} \\exp\\left(-\\frac{(x - \\mu)^2}{2\\sigma^2}\\right)\\\\
 * %&= \\exp\\left({iu\\mu - \\tfrac{1}{2}\\sigma^2 u^2}\\right)
 * \\end{align*}
 * }
 * and
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi_{X_t}(u) &= [\\phi_X(u)]^t \\\\
 * &= \\exp\\left(t\\left(iu\\mu - \\frac{\\sigma^2 u^2}{2}\\right)\\right)
 * \\end{align*}
 * }
 * 
 */
public class GaussianCharacteristicExponent implements CharacteristicExponent {
  private final double _mu;
  private final double _sigma;

  /**
   * 
   * @param mu The mean of the Gaussian distribution
   * @param sigma The standard deviation of the Gaussian distribution, not negative or zero
   */
  public GaussianCharacteristicExponent(final double mu, final double sigma) {
    Validate.isTrue(sigma > 0.0, "sigma > 0");
    _mu = mu;
    _sigma = sigma;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber>() {
      @Override
      public ComplexNumber evaluate(ComplexNumber x) {
        return getValue(x, t);
      }
    };
  }

  @Override
  public ComplexNumber getValue(ComplexNumber u, double t) {
    Validate.isTrue(t > 0.0, "t > 0");
    Validate.notNull(u, "u");
    final ComplexNumber temp = multiply(_sigma, u);
    final ComplexNumber res = add(multiply(u, new ComplexNumber(0, _mu)), multiply(-0.5, multiply(temp, temp)));
    return multiply(t, res);
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber[]>() {
      @Override
      public ComplexNumber[] evaluate(ComplexNumber x) {
        return getCharacteristicExponentAdjoint(x, t);
      }
    };
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(final ComplexNumber u, final double t) {
    final ComplexNumber[] res = new ComplexNumber[3];
    res[0] = getValue(u, t);
    res[1] = multiply(u, new ComplexNumber(0.0, t));
    res[2] = multiply(-_sigma * t, square(u));
    return res;
  }

  /**
   * 
   * @return {@latex.inline $\\infty$}
   */
  @Override
  public double getLargestAlpha() {
    return Double.POSITIVE_INFINITY;
  }

  /**
   * 
   * @return {@latex.inline $-\\infty$}
   */
  @Override
  public double getSmallestAlpha() {
    return Double.NEGATIVE_INFINITY;
  }

  /**
   * Gets the mean.
   * @return the mean
   */
  public double getMu() {
    return _mu;
  }

  /**
   * Gets the standard deviation.
   * @return the standard deviation
   */
  public double getSigma() {
    return _sigma;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mu);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigma);
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
    final GaussianCharacteristicExponent other = (GaussianCharacteristicExponent) obj;
    if (Double.doubleToLongBits(_mu) != Double.doubleToLongBits(other._mu)) {
      return false;
    }
    return Double.doubleToLongBits(_sigma) == Double.doubleToLongBits(other._sigma);
  }

}
