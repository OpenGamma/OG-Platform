/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.special.Gamma;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 * The natural logarithm of the Gamma function {@link GammaFunction}.
 * <p>
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/special/Gamma.html">Commons Math library implementation</a> 
 * of the log-Gamma function
 */
public class NaturalLogGammaFunction extends Function1D<Double, Double> {

  
  /**
   * @param x The argument of the function, must be greater than zero
   * @return The value of the function 
   */
  @Override
  public Double evaluate(final Double x) {
    Validate.isTrue(x > 0, "x must be greater than zero");
    return Gamma.logGamma(x);
  }
}
