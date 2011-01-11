/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.math.special.Gamma;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

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
    ArgumentChecker.notNegativeOrZero(x, "x");
    return Gamma.logGamma(x);
  }
}
