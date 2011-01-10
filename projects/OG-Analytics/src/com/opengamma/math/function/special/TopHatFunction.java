/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class TopHatFunction extends Function1D<Double, Double> {
  private final double _x1;
  private final double _x2;
  private final double _y;

  public TopHatFunction(final double x1, final double x2, final double y) {
    if (x1 > x2) {
      throw new IllegalArgumentException("x1 must be less than x2");
    }
    _x1 = x1;
    _x2 = x2;
    _y = y;
  }

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x, "x");
    if (ArgumentChecker.isInRangeInclusive(_x1, _x2, x)) {
      return _y;
    }
    return 0.;
  }

}
