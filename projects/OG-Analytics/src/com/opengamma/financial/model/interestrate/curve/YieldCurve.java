/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import com.opengamma.math.curve.Curve;

/**
 * 
 */
public class YieldCurve extends YieldAndDiscountCurve {

  public YieldCurve(final Curve<Double, Double> yieldCurve) {
    super(yieldCurve);
  }

  @Override
  public double getInterestRate(final Double t) {
    return getCurve().getYValue(t);
  }

  @Override
  public double getDiscountFactor(final Double t) {
    return Math.exp(-t * getInterestRate(t));
  }
}
