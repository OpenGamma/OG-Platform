/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 //TODO Review this
 * This exists so that yield curves construction (particularly with OIS) works correctly. It acts like a CouponFixed except for yield Curve sensitivities (PV and par rate) where it acts
 * like a CouponIbor, since even though the rate, and thus the payment, is fixed (in the case or Ibor, but not OIS), the payment is still sensitive to the curve from the point of view of yield
 * curve construction.
 */
public class CouponIborFixed extends CouponFixed {
  private final CouponIbor _couponIbor;

  public CouponIborFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate,
      final double fixingTime, IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double spread,
      final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, rate);
    _couponIbor = new CouponIbor(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime, index, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, spread,
        forwardCurveName);
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
    final CouponIborFixed other = (CouponIborFixed) obj;
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
  public String toString() {
    return super.toString() + _couponIbor.toString();

  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborFixed(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborFixed(this);
  }

}
