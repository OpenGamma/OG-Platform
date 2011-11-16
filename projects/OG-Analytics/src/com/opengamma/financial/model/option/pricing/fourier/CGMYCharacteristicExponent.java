/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.pow;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.I;
import static com.opengamma.math.number.ComplexNumber.ZERO;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.math.number.ComplexNumber;

/**
 * This class represents the characteristic function of the Carr-Madan-Geman-Yor (CGMY) process. This process is a pure jump process (i.e.
 * there is no Brownian component).
 * <p>
 * The characteristic function is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi(u; C, G, M, Y) = \\exp\\left(C \\Gamma(-Y)\\left[(M - iu)^Y - M^Y + (G + iu)^Y - G^Y\\right]\\right)
 * \\end{align*}
 * }
 */
public class CGMYCharacteristicExponent implements CharacteristicExponent {
  private static final GammaFunction GAMMA_FUNCTION = new GammaFunction();
  private final double _c;
  private final double _g;
  private final double _m;
  private final double _y;
  private final double _minAlpha;
  private final double _maxAlpha;
  private final double _r1;
  private final double _r2;
  private final double _r3;

  /**
   * The parameters for the CGMY process
   * @param c C, > 0
   * @param g G, > 0
   * @param m M, > 1
   * @param y Y, < 2
   */
  public CGMYCharacteristicExponent(final double c, final double g, final double m, final double y) {
    Validate.isTrue(c > 0, "C > 0");
    Validate.isTrue(g > 0, "G > 0");
    Validate.isTrue(m > 1, "M > 1");
    Validate.isTrue(y < 2, "Y < 2");
    _c = c;
    _g = g;
    _m = m;
    _y = y;
    _minAlpha = -(_g + 1.0);
    _maxAlpha = _m - 1.0;
    _r1 = Math.pow(_m, _y) + Math.pow(_g, _y);
    _r2 = _c * GAMMA_FUNCTION.evaluate(-_y);
    _r3 = (Math.pow(_m - 1, _y) + Math.pow(_g + 1, _y) - _r1);
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber>() {
      @Override
      public ComplexNumber evaluate(ComplexNumber u) {
        return getValue(u, t);
      }
    };
  }

  @Override
  public ComplexNumber getValue(ComplexNumber u, double t) {
    final double r2 = t * _r2;
    final ComplexNumber complexR3 = new ComplexNumber(r2 * _r3);
    if (u.getReal() == 0.0) {
      if (u.getImaginary() == 0.0) {
        return ZERO;
      }
      if (u.getImaginary() == -1.0) {
        return complexR3;
      }
    }
    final ComplexNumber iu = multiply(I, u);
    final ComplexNumber c1 = pow(subtract(_m, iu), _y);
    final ComplexNumber c2 = pow(add(_g, iu), _y);
    final ComplexNumber c3 = add(c1, c2);
    final ComplexNumber c4 = subtract(c3, _r1);
    final ComplexNumber res = multiply(r2, c4);
    return res;
  }

  /**
   * Gets C
   * @return C
   */
  public double getC() {
    return _c;
  }

  /**
   * Gets G
   * @return G
   */
  public double getG() {
    return _g;
  }

  /**
   * Gets M
   * @return M
   */
  public double getM() {
    return _m;
  }

  /**
   * Gets Y
   * @return Y
   */
  public double getY() {
    return _y;
  }

  /**
   * 
   * @return {@latex.inline $M - 1$}
   */
  @Override
  public double getLargestAlpha() {
    return _maxAlpha;
  }

  /**
   * 
   * @return {@latex.inline $-G - 1$}
   */
  @Override
  public double getSmallestAlpha() {
    return _minAlpha;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_c);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_g);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_m);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_y);
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
    final CGMYCharacteristicExponent other = (CGMYCharacteristicExponent) obj;
    if (Double.doubleToLongBits(_c) != Double.doubleToLongBits(other._c)) {
      return false;
    }
    if (Double.doubleToLongBits(_g) != Double.doubleToLongBits(other._g)) {
      return false;
    }
    if (Double.doubleToLongBits(_m) != Double.doubleToLongBits(other._m)) {
      return false;
    }
    return Double.doubleToLongBits(_y) == Double.doubleToLongBits(other._y);
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(ComplexNumber u, double t) {
    throw new NotImplementedException();
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(double t) {
    throw new NotImplementedException();
  }

}
