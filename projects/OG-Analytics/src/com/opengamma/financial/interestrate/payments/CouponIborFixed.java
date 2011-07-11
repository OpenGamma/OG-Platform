/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CouponIborFixed extends CouponFixed {
  private CouponIbor _couponIbor;


  public CouponIborFixed(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double rate, final double notional, final double fixingTime,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double spread, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, rate);
    _couponIbor = new CouponIbor(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, forwardCurveName);
  }

  public CouponIbor toCouponIbor() {
    return _couponIbor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_couponIbor == null) ? 0 : _couponIbor.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponIborFixed other = (CouponIborFixed) obj;
    if (_couponIbor == null) {
      if (other._couponIbor != null) {
        return false;
      }
    } else if (!_couponIbor.equals(other._couponIbor)) {
      return false;
    }
    return true;
  }
  
  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborFixed(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborFixed(this);
  }

}
