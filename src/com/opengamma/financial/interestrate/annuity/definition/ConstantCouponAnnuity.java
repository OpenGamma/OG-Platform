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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_couponRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConstantCouponAnnuity other = (ConstantCouponAnnuity) obj;
    if (Double.doubleToLongBits(_couponRate) != Double.doubleToLongBits(other._couponRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return true;
  }

}
