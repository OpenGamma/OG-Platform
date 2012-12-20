/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function2D;

/**
 * 
 */
public class LognormalSkewnessFromVolatilityCalculator extends Function2D<Double, Double> {

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    Validate.notNull(sigma, "sigma");
    Validate.notNull(t, "t");
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    return y * (3 + y * y);
  }

}
