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
public class LognormalPearsonKurtosisFromVolatilityCalculator extends Function2D<Double, Double> {
  private static final LognormalFisherKurtosisFromVolatilityCalculator CALCULATOR = new LognormalFisherKurtosisFromVolatilityCalculator();

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    Validate.notNull(sigma, "sigma");
    Validate.notNull(t, "t");
    return CALCULATOR.evaluate(sigma, t) + 3;
  }

}
