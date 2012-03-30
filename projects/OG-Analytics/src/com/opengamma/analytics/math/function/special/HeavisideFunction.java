/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Class representing the Heaviside step function, defined as:
 * $$
 * \begin{align*}
 * H(x) = 
 * \begin{cases}
 * 0 & \text{ when } x < 0\\
 * 1 & \text{ when } x > 0
 * \end{cases}
 * \end{align*}
 * $$
 * This function is discontinuous and is not defined for $x=0$.
 */
public class HeavisideFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x);
    if (x < 0) {
      return 0.;
    }
    if (x > 0) {
      return 1.;
    }
    throw new IllegalArgumentException("Heaviside function is not defined for x = 0");
  }

}
