/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * The implementation of a YieldAndDiscount curve where the curve is stored with maturities and zero-coupon continuously-compounded rates.
 */
public class YieldCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data in the zero-coupon continuously compounded convention.
   */
  private final DoublesCurve _curve;

  /** 
   * @param name The curve name.
   * @param yieldCurve Curve containing continuously-compounded rates against maturities. Rates are unitless (eg 0.02 for two percent) and maturities are in years.
   */
  public YieldCurve(final String name, final DoublesCurve yieldCurve) {
    super(name);
    ArgumentChecker.notNull(yieldCurve, "Curve");
    _curve = yieldCurve;
  }

  /**
   * Builder from a DoublesCurve using the name of the DoublesCurve as the name of the YieldCurve.
   * @param yieldCurve The underlying curve based on yields (continuously-compounded).
   * @return The yield curve.
   */
  public static YieldCurve from(final DoublesCurve yieldCurve) {
    ArgumentChecker.notNull(yieldCurve, "Curve");
    return new YieldCurve(yieldCurve.getName(), yieldCurve);
  }

  @Override
  public double getInterestRate(final Double t) {
    return getCurve().getYValue(t);
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double t) {
    return ArrayUtils.toPrimitive(_curve.getYValueParameterSensitivity(t));
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.getNumberOfParameters();
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
