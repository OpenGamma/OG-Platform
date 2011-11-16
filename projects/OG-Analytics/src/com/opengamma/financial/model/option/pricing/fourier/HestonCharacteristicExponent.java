/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import static com.opengamma.math.ComplexMathUtils.square;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.I;
import static com.opengamma.math.number.ComplexNumber.ZERO;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.smile.function.HestonModelData;
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
public class HestonCharacteristicExponent implements MartingaleCharacteristicExponent {
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

  public HestonCharacteristicExponent(final HestonModelData data) {
    Validate.notNull(data, "null data");
    _kappa = data.getKappa();
    _theta = data.getTheta();
    _vol0 = data.getVol0();
    _omega = data.getOmega();
    _rho = data.getRho();
    final double t1 = _omega - 2 * _kappa * _rho;
    final double rhoStar = 1 - _rho * _rho;
    final double root = Math.sqrt(t1 * t1 + 4 * _kappa * _kappa * rhoStar);
    _alphaMin = (t1 - root) / _omega / rhoStar - 1;
    _alphaMax = (t1 + root) / _omega / rhoStar + 1;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber>() {
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        return getValue(u, t);
      }
    };
  }

  @Override
  public ComplexNumber getValue(ComplexNumber u, double t) {
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

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber[]>() {
      @Override
      public ComplexNumber[] evaluate(final ComplexNumber u) {
        return getCharacteristicExponentAdjoint(u, t);
      }
    };
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(final ComplexNumber u, final double t) {
    ComplexNumber[] res = new ComplexNumber[6];

    final double kto = _kappa * _theta / _omega / _omega;

    final ComplexNumber rhoOmegaUi = multiply(u, new ComplexNumber(0, _rho * _omega));
    final ComplexNumber rhoOmegaUiMinusKappa = subtract(rhoOmegaUi, _kappa);
    final ComplexNumber rhoOmegaUiMinusKappa2 = square(rhoOmegaUiMinusKappa);
    final ComplexNumber uOmega2i = multiply(u, new ComplexNumber(0, _omega * _omega));
    final ComplexNumber omega2u2 = square(multiply(u, _omega));
    final ComplexNumber smallD2 = add(add(rhoOmegaUiMinusKappa2, uOmega2i), omega2u2);
    final ComplexNumber smallD = sqrt(smallD2);

    final ComplexNumber num = subtract(smallD, rhoOmegaUiMinusKappa);
    final ComplexNumber denom = multiply(-1.0, add(smallD, rhoOmegaUiMinusKappa));
    final ComplexNumber smallC = divide(num, denom);

    final ComplexNumber denomT = multiply(t, denom);
    final ComplexNumber e = exp(multiply(smallD, -t));
    final ComplexNumber arg1 = divide(subtract(smallC, e), subtract(smallC, 1));

    final ComplexNumber lnArg1 = log(arg1);
    final ComplexNumber arg2 = divide(subtract(1, e), subtract(smallC, e));

    final ComplexNumber bigC = multiply(kto, subtract(denomT, multiply(2, lnArg1)));
    final ComplexNumber bigD = divide(multiply(num, arg2), _omega * _omega);

    final ComplexNumber dv0 = multiply(_vol0, bigD);
    res[0] = add(bigC, dv0); //value of CE

    //backwards sweep
    final ComplexNumber bigCBar = new ComplexNumber(1.0);
    final ComplexNumber bigDBar = new ComplexNumber(_vol0);
    res[3] = bigD; //vol0

    final ComplexNumber bigDXarg2 = divide(num, _omega * _omega);
    final ComplexNumber arg2Bar = multiply(bigDBar, bigDXarg2);
    final ComplexNumber bigDXNum = divide(arg2, _omega * _omega);
    final ComplexNumber numBar1 = multiply(bigDBar, bigDXNum); //since num appears in several places in the graph we need to split it out 
    final ComplexNumber bigDXOmega = multiply(-2 / _omega, bigD);
    final ComplexNumber omegaBar1 = multiply(bigDBar, bigDXOmega);

    final ComplexNumber bigCXlnArg1 = new ComplexNumber(-2 * kto);
    final ComplexNumber lnArg1Bar = multiply(bigCBar, bigCXlnArg1);
    final ComplexNumber bigCXdenom = new ComplexNumber(t * kto);
    final ComplexNumber denomBar1 = multiply(bigCBar, bigCXdenom);
    final ComplexNumber bigCXkto = divide(bigC, kto);
    final ComplexNumber ktoBar = multiply(bigCBar, bigCXkto);

    final ComplexNumber arg2XsmallC = divide(arg2, subtract(e, smallC));
    final ComplexNumber smallCBar1 = multiply(arg2Bar, arg2XsmallC);
    final ComplexNumber arg2Xe = divide(subtract(1, smallC), square(subtract(smallC, e)));
    final ComplexNumber eBar1 = multiply(arg2Bar, arg2Xe);

    final ComplexNumber lnArg1Xarg1 = divide(1.0, arg1);
    final ComplexNumber arg1Bar = multiply(lnArg1Bar, lnArg1Xarg1);
    final ComplexNumber arg1XsmallC = divide(subtract(e, 1.0), square(subtract(smallC, 1.0)));
    final ComplexNumber smallCBar2 = multiply(arg1Bar, arg1XsmallC);
    final ComplexNumber arg1Xe = divide(1.0, subtract(1.0, smallC));
    final ComplexNumber eBar2 = multiply(arg1Bar, arg1Xe);

    final ComplexNumber eXsmallD = multiply(-t, e);
    final ComplexNumber smallDBar1 = multiply(add(eBar1, eBar2), eXsmallD);

    final ComplexNumber smallCXnum = divide(1.0, denom);
    final ComplexNumber smallCBar = add(smallCBar1, smallCBar2);
    final ComplexNumber numBar2 = multiply(smallCBar, smallCXnum);
    final ComplexNumber smallCXdenom = multiply(-1.0, divide(smallC, denom));
    final ComplexNumber denomBar2 = multiply(smallCBar, smallCXdenom);

    final ComplexNumber numBar = add(numBar1, numBar2);
    final ComplexNumber denomBar = add(denomBar1, denomBar2);
    final ComplexNumber smallDBar2 = numBar;
    final ComplexNumber smallDBar3 = multiply(-1.0, denomBar);
    final ComplexNumber smallDBar = add(smallDBar1, smallDBar2, smallDBar3);

    final ComplexNumber smallDXsmallD2 = divide(0.5, smallD);
    final ComplexNumber smallD2Bar = multiply(smallDBar, smallDXsmallD2);
    final ComplexNumber rhoOmegaUiMinusKappa2Bar = smallD2Bar;
    final ComplexNumber uOmega2iBar = smallD2Bar;
    final ComplexNumber omega2u2Bar = smallD2Bar;
    final ComplexNumber rhoOmegaUiMinusKappa2XrhoOmegaUiMinusKappa = multiply(2.0, rhoOmegaUiMinusKappa);
    final ComplexNumber rhoOmegaUiMinusKappaBar1 = multiply(rhoOmegaUiMinusKappa2Bar, rhoOmegaUiMinusKappa2XrhoOmegaUiMinusKappa);
    final ComplexNumber rhoOmegaUiMinusKappaBar2 = multiply(numBar, -1.0);
    final ComplexNumber rhoOmegaUiMinusKappaBar3 = multiply(denomBar, -1.0);
    final ComplexNumber rhoOmegaUiMinusKappaBar = add(rhoOmegaUiMinusKappaBar1, rhoOmegaUiMinusKappaBar2, rhoOmegaUiMinusKappaBar3);

    final ComplexNumber rhoOmegaUiBar = rhoOmegaUiMinusKappaBar;
    final ComplexNumber kappaBar1 = multiply(rhoOmegaUiMinusKappaBar, -1.0);
    final double ktoXkappa = kto / _kappa;
    final ComplexNumber kappaBar2 = multiply(ktoBar, ktoXkappa);
    res[1] = add(kappaBar1, kappaBar2); //kappaBar
    final double ktoXtheta = kto / _theta;
    res[2] = multiply(ktoBar, ktoXtheta); //thetaBar

    final double ktoXomega = -2.0 * kto / _omega;
    final ComplexNumber omegaBar2 = multiply(ktoBar, ktoXomega);
    final ComplexNumber rhoOmegaUiXomega = multiply(u, new ComplexNumber(0.0, _rho));
    final ComplexNumber omegaBar3 = multiply(rhoOmegaUiBar, rhoOmegaUiXomega);
    final ComplexNumber uOmega2iXomega = multiply(u, new ComplexNumber(0.0, 2 * _omega));
    final ComplexNumber omegaBar4 = multiply(uOmega2iBar, uOmega2iXomega);
    final ComplexNumber omega2u2Xomega = multiply(2 * _omega, square(u));
    final ComplexNumber omegaBar5 = multiply(omega2u2Bar, omega2u2Xomega);
    res[4] = add(omegaBar1, omegaBar2, omegaBar3, omegaBar4, omegaBar5);

    final ComplexNumber rhoOmegaUiXrho = multiply(u, new ComplexNumber(0.0, _omega));
    res[5] = multiply(rhoOmegaUiBar, rhoOmegaUiXrho);

    return res;
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
