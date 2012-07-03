/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.Curve;

/**
 * The implementation of a YieldAndDiscount curve where the curve is stored with maturities and zero-coupon continuously-compounded rates.
 */
public class YieldCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data in the zero-coupon continuously compounded convention.
   */
  private final Curve<Double, Double> _curve;

  /** 
   * @param yieldCurve Curve containing continuously-compounded rates against maturities. Rates are unitless (eg 0.02 for two percent) and maturities are in years.
   * TODO: Change the constructor to check for null yield curve.
   */
  public YieldCurve(final Curve<Double, Double> yieldCurve) {
    super(yieldCurve.getName());
    _curve = yieldCurve;
  }

  @Override
  public double getInterestRate(final Double t) {
    return getCurve().getYValue(t);
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double t) {
    return ArrayUtils.toPrimitive(_curve.getYValueParameterSensitivity(t));
  }

  /**
   * Gets the underlying curve. 
   * @return The curve.
   */
  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurve other = (YieldCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
