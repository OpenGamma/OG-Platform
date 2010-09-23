/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * 
 */
public class ConstantCouponAnnuity extends FixedAnnuity {

  public ConstantCouponAnnuity(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, 1.0, couponRate, yieldCurveName);
  }

  public ConstantCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName) {
    super(paymentTimes, notional, couponRate, yieldCurveName);
  }

  public ConstantCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    super(paymentTimes, notional, couponRate, yearFractions, yieldCurveName);
  }

  public double getCouponRate() {
    return getCoupons()[0]; // all coupons are the same value
  }

  @Override
  public ConstantCouponAnnuity withRate(double rate) {
    return new ConstantCouponAnnuity(getPaymentTimes(), getNotional(), rate, getYearFractions(), getFundingCurveName());
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitConstantCouponAnnuity(this, data);
  }

}
