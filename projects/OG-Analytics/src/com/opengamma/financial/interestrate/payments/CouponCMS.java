/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Constant Maturity Swap coupon.
 */
public class CouponCMS extends CouponFloating {

  /**
   * Swap underlying the CMS definition. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap;
  /**
   * The time (in years) to underlying swap settlement.
   */
  private final double _settlementTime;

  /**
   * Constructor from floating coupon details and underlying swap.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param settlementTime The time (in years) to underlying swap settlement.
   */
  public CouponCMS(Currency currency, double paymentTime, double paymentYearFraction, double notional, double fixingTime, FixedCouponSwap<? extends Payment> underlyingSwap, double settlementTime) {
    super(currency, paymentTime, underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName(), paymentYearFraction, notional, fixingTime);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
  }

  /**
   * Builder from a floating coupon and an underlying swap.
   * @param coupon A floating coupon.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @param settlementTime  The time (in years) to swap settlement.
   * @return The CMS coupon.
   */
  public static CouponCMS from(CouponFloating coupon, FixedCouponSwap<? extends Payment> underlyingSwap, double settlementTime) {
    Validate.notNull(coupon, "floating coupon");
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMS(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), underlyingSwap, settlementTime);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the underlying swap settlement time.
   * @return The swap settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  @Override
  public CouponCMS withNotional(double notional) {
    return new CouponCMS(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _underlyingSwap, _settlementTime);
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
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponCMS(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponCMS(this);
  }

}
