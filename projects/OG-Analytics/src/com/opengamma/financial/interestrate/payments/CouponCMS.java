/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;

/**
 * Class describing a Constant Maturity Swap coupon.
 */
public class CouponCMS extends CouponFloating {

  /**
   * Swap underlying the CMS definition. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap;

  /**
   * Constructor from floating coupon details and underlying swap.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   */
  public CouponCMS(double paymentTime, double paymentYearFraction, double notional, double fixingTime, FixedCouponSwap<? extends Payment> underlyingSwap) {
    super(paymentTime, underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName(), paymentYearFraction, notional, fixingTime);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
    _underlyingSwap = underlyingSwap;
  }

  /**
   * Builder from a floating coupon and an underlying swap.
   * @param coupon A floating coupon.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @return The CMS coupon.
   */
  public static CouponCMS from(CouponFloating coupon, FixedCouponSwap<Payment> underlyingSwap) {
    Validate.notNull(coupon, "floating coupon");
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMS(coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), underlyingSwap);
  }

  /**
   * Gets the _underlyingSwap field.
   * @return the _underlyingSwap
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingSwap.hashCode();
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
    CouponCMS other = (CouponCMS) obj;
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponCMS(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponCMS(this);
  }

}
