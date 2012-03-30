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
public class LognormalFisherKurtosisFromVolatilityCalculator extends Function2D<Double, Double> {

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    Validate.notNull(sigma, "sigma");
    Validate.notNull(t, "t");
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double y2 = y * y;
    return y2 * (16 + y2 * (15 + y2 * (6 + y2)));
  }

}
