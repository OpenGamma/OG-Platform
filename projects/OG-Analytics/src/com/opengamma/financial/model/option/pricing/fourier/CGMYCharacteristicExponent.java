/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class CGMYCharacteristicExponent extends CharacteristicExponent {
  private final double _c;
  private final double _g;
  private final double _m;
  private final double _y;
  private final double _t;
  private final double _r1;
  private final double _r2;
  private final double _r3;
  private final ComplexNumber _complexR3;

  public CGMYCharacteristicExponent(final double c, final double g, final double m, final double y, final double t) {
    Validate.isTrue(c > 0, "C > 0");
    Validate.isTrue(g > 0, "G > 0");
    Validate.isTrue(m > 1, "M > 1");
    Validate.isTrue(y < 2, "Y < 2");
    Validate.isTrue(t > 0, "t > 0");

    _c = c;
    _g = g;
    _m = m;
    _y = y;
    _t = t;

    _r1 = Math.pow(m, y) + Math.pow(g, y);
    _r2 = t * c * (new GammaFunction()).evaluate(-y);
    _r3 = _r2 * (Math.pow(m - 1, y) + Math.pow(g + 1, y) - _r1);
    _complexR3 = new ComplexNumber(_r3);
  }

  @Override
  public ComplexNumber evaluate(final ComplexNumber x) {

    //that u = 0 gives zero is true for any characteristic function
    if (x.getReal() == 0.0) {
      if (x.getImaginary() == 0.0) {
        return ZERO;
      }
      if (x.getImaginary() == -1.0) {
        return _complexR3;
      }
    }

    final ComplexNumber ix = multiply(I, x);
    final ComplexNumber c1 = pow(subtract(_m, ix), _y);
    final ComplexNumber c2 = pow(add(_g, ix), _y);
    final ComplexNumber c3 = add(c1, c2);
    final ComplexNumber c4 = subtract(c3, _r1);
    final ComplexNumber res = multiply(_r2, c4);
    return res;
  }

  public double getC() {
    return _c;
  }

  public double getG() {
    return _g;
  }

  public double getM() {
    return _m;
  }

  public double getY() {
    return _y;
  }

  @Override
  public double getTime() {
    return _t;
  }

  @Override
  public double getLargestAlpha() {
    return _m - 1.0;
  }

  @Override
  public double getSmallestAlpha() {
    return -(_g + 1.0);
  }

}
