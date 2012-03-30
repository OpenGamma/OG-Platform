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
public class YieldCurve extends YieldAndDiscountCurve {

  /** 
   * @param yieldCurve Curve containing continuously-compounded rates against maturities. Rates are unitless (eg 0.02 for two percent) and maturities are in years.
   */
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
