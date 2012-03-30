/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.InterestRateModel;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;

/**
 * A DiscountCurve contains discount factors <i>e<sup>-r(t)t</sup></i> (where
 * <i>t</i> is the maturity in years and <i>r(t)</i> is the continuously-compounded interest rate to
 * maturity <i>t</i>).
 */

public abstract class YieldAndDiscountCurve  implements InterestRateModel<Double> {
  private final Curve<Double, Double> _curve;

  public YieldAndDiscountCurve(final Curve<Double, Double> curve) {
    Validate.notNull(curve, "curve");
    _curve = curve;
  }

  /**
   * @param t The time 
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public abstract double getInterestRate(final Double t);

  /**
   * @param t The time 
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  public abstract double getDiscountFactor(final Double t);

  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  public YieldAndDiscountCurve withParallelShift(final double shift) {
    return new YieldCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, shift));
  }

  public YieldAndDiscountCurve withSingleShift(final double t, final double shift) {
    return new YieldCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, t, shift));
  }

  public YieldAndDiscountCurve withMultipleShifts(final double[] xShifts, final double[] yShifts) {
    return new YieldCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, xShifts, yShifts));
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
    final YieldAndDiscountCurve other = (YieldAndDiscountCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
