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

  private ComplexNumber[] forwardSweep(final ComplexNumber u, final double t) {
    ComplexNumber[] w = new ComplexNumber[19];
    w[0] = new ComplexNumber(_kappa * _theta / _omega / _omega);
    w[1] = multiply(u, new ComplexNumber(0, _rho * _omega));
    w[2] = subtract(w[1], _kappa);
    w[3] = square(w[2]);
    w[4] = multiply(u, new ComplexNumber(0, _omega * _omega));
    w[5] = square(multiply(u, _omega));
    w[6] = add(w[3], w[4], w[5]);
    w[7] = sqrt(w[6]);
    w[8] = subtract(w[7], w[2]);
    w[9] = multiply(-1.0, add(w[7], w[2]));
    w[10] = divide(w[8], w[9]);
    w[11] = multiply(t, w[9]);
    w[12] = exp(multiply(w[7], -t));
    w[13] = divide(subtract(w[10], w[12]), subtract(w[10], 1));
    w[14] = log(w[13]);
    w[15] = divide(subtract(1, w[12]), subtract(w[10], w[12]));
    w[16] = multiply(w[0], subtract(w[11], multiply(2, w[14])));
    w[17] = divide(multiply(w[8], w[15]), _omega * _omega);
    w[18] = add(w[16], multiply(_vol0, w[17]));
    return w;
  }

  private ComplexNumber[] backwardsSweep(final ComplexNumber[] w, final double t) {
    final double oneOverOmega2 = 1 / _omega / _omega;
    final ComplexNumber[] wBar = new ComplexNumber[19];
    wBar[18] = new ComplexNumber(1.0); //formal start
    wBar[17] = new ComplexNumber(_vol0); //Suppressing the multiple by wBar18
    wBar[16] = wBar[18];
    wBar[15] = multiply(oneOverOmega2, w[8], wBar[17]);
    wBar[14] = multiply(-2, w[0], wBar[16]);
    wBar[13] = divide(wBar[14], w[13]);
    ComplexNumber temp1 = subtract(1.0, w[10]);
    ComplexNumber temp2 = subtract(w[10], w[12]);
    ComplexNumber temp3 = square(temp2);
    wBar[12] = add(multiply(wBar[15], divide(temp1, temp3)), divide(wBar[13], temp1));
    wBar[11] = multiply(w[0], wBar[16]);
    wBar[10] = add(multiply(divide(subtract(w[12], 1), temp3), wBar[15]), multiply(divide(subtract(w[12], 1), square(temp1)), wBar[13]));
    wBar[9] = subtract(multiply(t, wBar[11]), multiply(divide(w[8], square(w[9])), wBar[10]));
    wBar[8] = add(multiply(oneOverOmega2, w[15], wBar[17]), divide(wBar[10], w[9]));
    //subtract(add(multiply(wBar12, multiply(-t, w12)), wBar8), wBar9);
    wBar[7] = subtract(wBar[8], add(multiply(t, w[12], wBar[12]), wBar[9]));
    wBar[6] = divide(wBar[7], multiply(2, w[7]));
    wBar[5] = wBar[6];
    wBar[4] = wBar[6];
    wBar[3] = wBar[6];
    wBar[2] = subtract(multiply(2, w[2], wBar[3]), add(wBar[8], wBar[9]));
    wBar[1] = wBar[2];
    wBar[0] = multiply(subtract(w[11], multiply(2, w[14])), wBar[16]);

    return wBar;
  }

  public ComplexNumber[] getCharacteristicExponentAdjointDebug(final ComplexNumber u, final double t) {
    ComplexNumber[] res = new ComplexNumber[6];
    ComplexNumber[] w = forwardSweep(u, t);
    ComplexNumber[] wBar = backwardsSweep(w, t);

    res[0] = w[18];
    res[1] = subtract(multiply(_theta / _omega / _omega, wBar[0]), wBar[2]);
    res[2] = multiply(_kappa / _omega / _omega, wBar[0]);
    res[3] = multiply(w[17], wBar[18]);
    res[4] = multiply(1 / _omega, add(multiply(-2, w[0], wBar[0]), multiply(w[1], wBar[1]), multiply(2, w[4], wBar[4]), multiply(2, w[5], wBar[5]),
        multiply(-2, w[17], wBar[17])));
    res[5] = multiply(1 / _rho, w[1], wBar[1]);

    return res;
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(final ComplexNumber u, final double t) {
    ComplexNumber[] res = new ComplexNumber[6];

    if (u.getReal() == 0.0 && (u.getImaginary() == 0.0 || u.getImaginary() == -1.0)) {
      for (int i = 0; i < 6; i++) {
        res[i] = ZERO;
      }
      return res;
    }

    //non-stochastic vol limit
    if (_omega == 0.0 || mod(multiply(_omega / _kappa, u, add(I, u))) < 1e-6) {
      final ComplexNumber z = multiply(u, add(I, u));

      //      //TODO calculate the omega -> 0 sensitivity without resorting to this hack
      //      HestonCharacteristicExponent ceTemp = this.withOmega(1.1e-6 * _kappa / mod(z));
      //      ComplexNumber[] temp = ceTemp.getCharacteristicExponentAdjoint(u, t);

      final double var;
      if (_kappa * t < 1e-6) {
        var = _vol0 * t + (_vol0 - _theta) * _kappa * t * t / 2;
        res[1] = multiply(-0.5 * (_vol0 - _theta) * t * t / 2, z);
        res[2] = multiply(_kappa * t * t / 4, z);
        res[3] = multiply(-0.5 * (t + _kappa * t * t / 2), z);
        res[4] = ZERO; //TODO this is wrong
        res[5] = ZERO;
      } else {
        final double expKappaT = Math.exp(-_kappa * t);
        var = _theta * t + (_vol0 - _theta) * (1 - expKappaT) / _kappa;
        res[1] = multiply(-0.5 * (_vol0 - _theta) * (expKappaT * (1 + t * _kappa) - 1) / _kappa / _kappa, z);
        res[2] = multiply(-0.5 * (t - (1 - expKappaT) / _kappa), z);
        res[3] = multiply(-0.5 * (1 - expKappaT) / _kappa, z);
        res[4] = ZERO; //TODO this is wrong
        res[5] = ZERO;
      }
      res[0] = multiply(-var / 2, z);
      return res;
    }

    final double oneOverOmega2 = 1 / _omega / _omega;
    final double w0 = _kappa * _theta * oneOverOmega2;

    final ComplexNumber w1 = multiply(u, new ComplexNumber(0, _rho * _omega)); //w1
    final ComplexNumber w2 = subtract(w1, _kappa); //w2
    final ComplexNumber w3 = square(w2); //w3
    final ComplexNumber w4 = multiply(u, new ComplexNumber(0, _omega * _omega));
    final ComplexNumber w5 = square(multiply(u, _omega));
    final ComplexNumber w6 = add(w3, w4, w5);
    final ComplexNumber w7 = sqrt(w6);

    final ComplexNumber w8 = subtract(w7, w2);
    final ComplexNumber w9 = multiply(-1.0, add(w7, w2));
    final ComplexNumber w10 = divide(w8, w9);

    final ComplexNumber w11 = multiply(t, w9);
    final ComplexNumber w12 = exp(multiply(w7, -t));
    final ComplexNumber w13 = divide(subtract(w10, w12), subtract(w10, 1));

    final ComplexNumber w14 = log(w13);
    final ComplexNumber w15 = divide(subtract(1, w12), subtract(w10, w12));

    final ComplexNumber w16 = multiply(w0, subtract(w11, multiply(2, w14)));
    final ComplexNumber w17 = multiply(oneOverOmega2, w8, w15);
    final ComplexNumber w18 = add(w16, multiply(_vol0, w17));

    res[0] = w18; //value of CE

    //backwards sweep
    final ComplexNumber wBar16 = new ComplexNumber(1.0);
    final ComplexNumber wBar17 = new ComplexNumber(_vol0);
    final ComplexNumber wBar15 = multiply(oneOverOmega2, wBar17, w8);
    final ComplexNumber wBar14 = multiply(-2 * w0, wBar16);
    final ComplexNumber wBar13 = divide(wBar14, w13);
    final ComplexNumber wBar12a = multiply(wBar15, divide(subtract(1, w10), square(subtract(w10, w12))));
    final ComplexNumber wBar12b = multiply(wBar13, divide(1.0, subtract(1.0, w10)));
    final ComplexNumber wBar12 = add(wBar12a, wBar12b);
    final ComplexNumber wBar11 = multiply(w0, wBar16);
    final ComplexNumber wBar10a = multiply(wBar15, divide(w15, subtract(w12, w10)));
    final ComplexNumber wBar10b = multiply(wBar13, divide(subtract(w12, 1.0), square(subtract(w10, 1.0))));
    final ComplexNumber wBar10 = add(wBar10a, wBar10b);
    final ComplexNumber wBar9a = multiply(t, wBar11);
    final ComplexNumber wBar9b = multiply(-1.0, wBar10, divide(w10, w9));
    final ComplexNumber wBar9 = add(wBar9a, wBar9b);
    final ComplexNumber wBar8a = multiply(oneOverOmega2, wBar17, w15);
    final ComplexNumber wBar8b = divide(wBar10, w9);
    final ComplexNumber wBar8 = add(wBar8a, wBar8b);
    final ComplexNumber wBar7 = subtract(add(multiply(wBar12, multiply(-t, w12)), wBar8), wBar9);
    final ComplexNumber wBar6 = multiply(wBar7, divide(0.5, w7));
    final ComplexNumber wBar5 = wBar6;
    final ComplexNumber wBar4 = wBar6;
    final ComplexNumber wBar3 = wBar6;
    final ComplexNumber wBar2 = subtract(multiply(wBar3, multiply(2.0, w2)), add(wBar8, wBar9));
    final ComplexNumber wBar1 = wBar2;
    final ComplexNumber wBar0 = multiply(wBar16, subtract(w11, multiply(2, w14)));

    res[1] = subtract(multiply(wBar0, _theta / _omega / _omega), wBar2); //kappaBar
    res[2] = multiply(wBar0, _kappa / _omega / _omega); //thetaBar
    res[3] = w17; //vol0

    final ComplexNumber omegaBar1 = multiply(-2 / _omega, w17, wBar17);
    final ComplexNumber omegaBar2 = multiply(-2.0 * w0 / _omega, wBar0);
    final ComplexNumber omegaBar3 = multiply(1 / _omega, w1, wBar1);
    final ComplexNumber omegaBar4 = multiply(2 / _omega, w4, wBar4);
    final ComplexNumber omegaBar5 = multiply(2 / _omega, w5, wBar5);
    res[4] = add(omegaBar1, omegaBar2, omegaBar3, omegaBar4, omegaBar5);

    res[5] = multiply(_omega, u, I, wBar1);

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

  public HestonCharacteristicExponent withKappa(final double kappa) {
    return new HestonCharacteristicExponent(kappa, _theta, _vol0, _omega, _rho);
  }

  public HestonCharacteristicExponent withTheta(final double theta) {
    return new HestonCharacteristicExponent(_kappa, theta, _vol0, _omega, _rho);
  }

  public HestonCharacteristicExponent withVol0(final double vol0) {
    return new HestonCharacteristicExponent(_kappa, _theta, vol0, _omega, _rho);
  }

  public HestonCharacteristicExponent withOmega(final double omega) {
    return new HestonCharacteristicExponent(_kappa, _theta, _vol0, omega, _rho);
  }

  public HestonCharacteristicExponent withRho(final double rho) {
    return new HestonCharacteristicExponent(_kappa, _theta, _vol0, _omega, rho);
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
