/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class ConstantCouponAnnuity extends FixedAnnuity {

  private final double _couponRate;
  private final double _notional;

  public ConstantCouponAnnuity(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, 1.0, couponRate, yieldCurveName);
  }

  public ConstantCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName) {
    super(paymentTimes, notional, couponRate, yieldCurveName);
    _couponRate = couponRate;
    _notional = notional;
  }

  public ConstantCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    super(paymentTimes, notional, couponRate, yearFractions, yieldCurveName);
    _couponRate = couponRate;
    _notional = notional;
  }

  public double getCouponRate() {
    return _couponRate;
  }

  @Override
  public double getNotional() {
    return _notional;
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<T> visitor, final YieldCurveBundle curves) {
    return visitor.visitConstantCouponAnnuity(this, curves);
  }

}
