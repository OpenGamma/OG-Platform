/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import com.opengamma.analytics.math.curve.Curve;

/**
 * 
 */
public class DiscountCurve extends YieldAndDiscountCurve {

  public DiscountCurve(final Curve<Double, Double> discountFactorCurve) {
    super(discountFactorCurve);
  }

  @Override
  public double getInterestRate(final Double t) {
    return -Math.log(getDiscountFactor(t)) / t;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    return getCurve().getYValue(t);
  }

}
