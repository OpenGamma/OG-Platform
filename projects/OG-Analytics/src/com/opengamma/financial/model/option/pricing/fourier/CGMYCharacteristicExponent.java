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
  private final double _r1;
  private final double _r2;

  public CGMYCharacteristicExponent(final double c, final double g, final double m, final double y) {
    Validate.isTrue(c > 0, "C > 0");
    Validate.isTrue(g > 0, "G > 0");
    Validate.isTrue(m > 1, "M > 1");
    Validate.isTrue(y < 2, "Y < 2");

    _c = c;
    _g = g;
    _m = m;
    _y = y;

    _r1 = Math.pow(m, y) + Math.pow(g, y);
    _r2 = c * (new GammaFunction()).evaluate(-y);
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber x, double t) {

    ComplexNumber ix = multiply(I, x);
    ComplexNumber c1 = pow(subtract(_m, ix), _y);
    ComplexNumber c2 = pow(add(_g, ix), _y);
    ComplexNumber c3 = add(c1, c2);
    ComplexNumber c4 = subtract(c3, _r1);
    ComplexNumber res = multiply(t, multiply(_r2, c4));
    return res;
  }
}
