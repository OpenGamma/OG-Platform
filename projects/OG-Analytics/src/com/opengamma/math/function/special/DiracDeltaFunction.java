/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class DiracDeltaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x, "x");
    return CompareUtils.closeEquals(x, 0, 1e-16) ? Double.POSITIVE_INFINITY : 0;
  }

}
