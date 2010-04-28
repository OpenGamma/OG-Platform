/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.math.special.Gamma;

import com.opengamma.math.function.Function1D;

/**
 * 
 * The natural logarithm of the Gamma function {@link GammaFunction}.
 * <p>
 * This class is a wrapper for the Commons Math library implementation of the logGamma function <a href="http://commons.apache.org/math/api-2.1/index.html">
 * 
 */

public class NaturalLogGammaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    if (x <= 0)
      throw new IllegalArgumentException("x must be greater than zero");
    return Gamma.logGamma(x);
  }
}
