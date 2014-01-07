/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Constant Maturity Swap coupon.
 */
public class CouponCMS extends CouponFloating {

  /**
   * Swap underlying the CMS definition. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final SwapFixedCoupon<? extends Payment> _underlyingSwap;
  /**
   * The time (in years) to underlying swap settlement.
   */
  private final double _settlementTime;

  /**
   * Constructor from floating coupon details and underlying swap.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName The funding curve name
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param settlementTime The time (in years) to underlying swap settlement.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponCMS(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional,
      final double fixingTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
  }

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
  public CouponCMS(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional,
      final double fixingTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
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
  @SuppressWarnings("deprecation")
  public static CouponCMS from(final CouponFloating coupon, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime) {
    ArgumentChecker.notNull(coupon, "floating coupon");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    try {
      return new CouponCMS(coupon.getCurrency(), coupon.getPaymentTime(), underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName(),
          coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), underlyingSwap, settlementTime);
    } catch (final IllegalStateException e) {
      return new CouponCMS(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(),
          coupon.getFixingTime(), underlyingSwap, settlementTime);
    }
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedCoupon<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the underlying swap settlement time.
   * @return The swap settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponCMS withNotional(final double notional) {
    try {
      return new CouponCMS(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional,
          getFixingTime(), _underlyingSwap, _settlementTime);
    } catch (final IllegalStateException e) {
      return new CouponCMS(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _underlyingSwap, _settlementTime);
    }
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponCMS(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponCMS(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_underlyingSwap == null) ? 0 : _underlyingSwap.hashCode());
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
    if (!(obj instanceof CouponCMS)) {
      return false;
    }
    final CouponCMS other = (CouponCMS) obj;
    if (Double.compare(_settlementTime, other._settlementTime) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
