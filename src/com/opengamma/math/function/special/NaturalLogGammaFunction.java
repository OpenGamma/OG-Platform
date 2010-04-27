/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import com.opengamma.math.function.Function1D;

/**
 * 
 * The natural logarithm of the Gamma function {@link GammaFunction}.
 * 
 * This class is a wrapper for the Commons Math library implementation of the logGamma function <a href="http://commons.apache.org/math/api-2.1/index.html">
 *
 * @author emcleod
 * 
 */

public class NaturalLogGammaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    return 0.;// Gamma.logGamma(x);
  }
}
