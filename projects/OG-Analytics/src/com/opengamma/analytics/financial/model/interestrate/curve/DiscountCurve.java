/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;

/**
 * 
 */
public class DiscountCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data as discount factors.
   */
  private final Curve<Double, Double> _curve;

  /**
   * Constructor from a curve containing the discount factors.
   * @param discountFactorCurve The curve.
   */
  public DiscountCurve(final Curve<Double, Double> discountFactorCurve) {
    super(discountFactorCurve.getName());
    _curve = discountFactorCurve;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    return _curve.getYValue(t);
  }

  /**
   * Gets the underlying curve. 
   * @return The curve.
   */
  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  @Override
  public YieldCurve withParallelShift(final double shift) {
    return new YieldCurve(CurveShiftFunctionFactory.getShiftedCurve(getCurve(), shift));
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
    final DiscountCurve other = (DiscountCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
