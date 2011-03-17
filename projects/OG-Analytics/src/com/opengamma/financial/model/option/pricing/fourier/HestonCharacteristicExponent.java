/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.log;
import static com.opengamma.math.ComplexMathUtils.mod;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.sqrt;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.I;
import static com.opengamma.math.number.ComplexNumber.ZERO;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * The Heston stochastic volatility model is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * dS_t &= \\mu S_t dt + \\sqrt{V_t}S_t dW_S(t)\\\\
 * dV_t &= \\kappa(\\theta - V_0)dt + \\omega\\sqrt{V_t} dW_V(t)\\\\
 * \\text{with}\\\\
 * dW_S(t) dW_V(t) = \\rho dt
 * \\end{align*}
 * }
 * This class represents the characteristic function of the Heston model:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi(u) &= e^{C(t, u) + D(t, u)V_0 + iu\\ln F}\\\\
 * \\text{where}\\\\
 * C(t, u) &= \\frac{\\kappa\\theta}{\\omega^2} \\left((\\kappa - \\rho \\omega ui + d(u))t - 2\\ln\\left(\\frac{c(u)e^{d(u)t} - 1}{c(u) - 1}\\right) \\right)\\\\
 * D(t, u) &= \\frac{\\kappa - \\rho \\omega ui + d(u)}{\\omega^2}\\left(\\frac{e^{d(u)t} - 1}{c(u)e^{d(u)t} - 1}\\right)\\\\ 
 * c(u) &= \\frac{\\kappa - \\rho \\omega ui + d(u)}{\\kappa - \\rho \\omega ui - d(u)}\\\\
 * \\text{and}\\\\
 * d(u) &= \\sqrt{(\\rho \\omega ui - \\kappa)^2 + iu\\omega^2 + \\omega^2 u^2}
 * \\end{align*}
 * }
 */
public class HestonCharacteristicExponent implements CharacteristicExponent {
  private final double _kappa;
  private final double _theta;
  private final double _vol0;
  private final double _omega;
  private final double _rho;
  private final double _alphaMin;
  private final double _alphaMax;

  /**
   * 
   * @param kappa mean-reverting speed 
   * @param theta mean-reverting level 
   * @param vol0 starting value of volatility
   * @param omega volatility-of-volatility
   * @param rho correlation between the spot process and the volatility process
   */
  //TODO check value of rho?
  public HestonCharacteristicExponent(final double kappa, final double theta, final double vol0, final double omega, final double rho) {
    _kappa = kappa;
    _theta = theta;
    _vol0 = vol0;
    _omega = omega;
    _rho = rho;
    final double t1 = _omega - 2 * _kappa * _rho;
    final double rhoStar = 1 - _rho * _rho;
    final double root = Math.sqrt(t1 * t1 + 4 * _kappa * _kappa * rhoStar);
    _alphaMin = (t1 - root) / _omega / rhoStar - 1;
    _alphaMax = (t1 + root) / _omega / rhoStar + 1;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {

    return new Function1D<ComplexNumber, ComplexNumber>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        // that u = 0 gives zero is true for any characteristic function, that u = -i gives zero is because this is already mean corrected
        if (u.getReal() == 0.0 && (u.getImaginary() == 0.0 || u.getImaginary() == -1.0)) {
          return ZERO;
        }

        //non-stochastic vol limit 
        if (_omega == 0.0 || mod(multiply(multiply(_omega / _kappa, u), add(I, u))) < 1e-6) {
          final ComplexNumber z = multiply(u, add(I, u));
          if (_kappa * t < 1e-6) {
            return multiply(-_vol0 / 2 * t, z);
          }
          final double var = _theta * t + (_vol0 - _theta) * (1 - Math.exp(-_kappa * t)) / _kappa;
          return multiply(-var / 2, z);
        }

        final ComplexNumber c = getC(u, t);
        final ComplexNumber dv0 = multiply(_vol0, getD(u, t));
        return add(c, dv0);
      }
    };
  }

  private ComplexNumber getC(final ComplexNumber u, final double t) {
    final ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    final ComplexNumber c = c(u);
    final ComplexNumber d = d(u);
    final ComplexNumber c3 = multiply(t, subtract(_kappa, add(d, c1)));
    final ComplexNumber e = exp(multiply(d, -t));
    ComplexNumber c4 = divide(subtract(c, e), subtract(c, 1));
    c4 = log(c4);
    return multiply(_kappa * _theta / _omega / _omega, subtract(c3, multiply(2, c4)));
  }

  private ComplexNumber getD(final ComplexNumber u, final double t) {
    final ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    final ComplexNumber c = c(u);
    final ComplexNumber d = d(u);
    final ComplexNumber c3 = add(_kappa, subtract(d, c1));
    final ComplexNumber e = exp(multiply(d, -t));
    final ComplexNumber c4 = divide(subtract(1, e), subtract(c, e));
    return divide(multiply(c3, c4), _omega * _omega);
  }

  private ComplexNumber c(final ComplexNumber u) {
    final ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    final ComplexNumber c2 = d(u);
    final ComplexNumber num = add(_kappa, subtract(c2, c1));
    final ComplexNumber denom = subtract(_kappa, add(c2, c1));
    return divide(num, denom);
  }

  private ComplexNumber d(final ComplexNumber u) {
    ComplexNumber c1 = subtract(multiply(u, new ComplexNumber(0, _rho * _omega)), _kappa);
    c1 = multiply(c1, c1);
    final ComplexNumber c2 = multiply(u, new ComplexNumber(0, _omega * _omega));
    ComplexNumber c3 = multiply(u, _omega);
    c3 = multiply(c3, c3);
    final ComplexNumber c4 = add(add(c1, c2), c3);
    return multiply(1, sqrt(c4));
  }

  /**
   * The maximum allowable value of {@latex.inline $alpha$} which is given by
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{align*}
   * t &= \\omega - 2 \\kappa \\rho \\\\
   * \\rho^* &= 1 - \\rho^2\\\\
   * r &= \\sqrt{t^2 + 4\\kappa^2\\rho^*}\\\\
   * \\alpha_{max} &= \\frac{t + r}{\\omega(\\rho^* - 1)}
   * \\end{align*}
   * }
   * 
   * @return {@latex.inline $alpha_{max}$}
   */
  @Override
  public double getLargestAlpha() {
    return _alphaMax;
  }

  /** The maximum allowable value of {@latex.inline $alpha$} which is given by
  * {@latex.ilb %preamble{\\usepackage{amsmath}}
  * \\begin{align*}
  * t &= \\omega - 2 \\kappa \\rho \\\\
  * \\rho^* &= 1 - \\rho^2\\\\
  * r &= \\sqrt{t^2 + 4\\kappa^2\\rho^*}\\\\
  * \\alpha_{min} &= \\frac{t - r}{\\omega(\\rho^* - 1)}
  * \\end{align*}
  * }
  * 
  * @return {@latex.inline $alpha_{min}$}
  */
  @Override
  public double getSmallestAlpha() {
    return _alphaMin;
  }

  /**
   * Gets the mean-reverting speed.
   * @return kappa
   */
  public double getKappa() {
    return _kappa;
  }

  /**
   * Gets the mean-reverting level.
   * @return theta
   */
  public double getTheta() {
    return _theta;
  }

  /**
   * Gets the initial volatility
   * @return initial volatility
   */
  public double getVol0() {
    return _vol0;
  }

  /**
   * Gets the volatility-of-volatility.
   * @return omega
   */
  public double getOmega() {
    return _omega;
  }

  /**
   * Gets the correlation.
   * @return rho
   */
  public double getRho() {
    return _rho;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_kappa);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_omega);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_theta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_vol0);
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
    final HestonCharacteristicExponent other = (HestonCharacteristicExponent) obj;
    if (Double.doubleToLongBits(_kappa) != Double.doubleToLongBits(other._kappa)) {
      return false;
    }
    if (Double.doubleToLongBits(_omega) != Double.doubleToLongBits(other._omega)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_theta) != Double.doubleToLongBits(other._theta)) {
      return false;
    }
    return Double.doubleToLongBits(_vol0) == Double.doubleToLongBits(other._vol0);
  }

}
