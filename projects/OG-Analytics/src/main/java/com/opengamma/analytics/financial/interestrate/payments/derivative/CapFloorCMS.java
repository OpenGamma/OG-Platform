/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS rate.
 */
public class CapFloorCMS extends CouponFloating implements CapFloor {

  /**
   * Swap underlying the CMS definition. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final SwapFixedCoupon<? extends Payment> _underlyingSwap;
  /**
   * The time (in years) to underlying swap settlement.
   */
  private final double _settlementTime;
  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Constructor from floating coupon details and underlying swap.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param fundingCurveName The funding curve name, not null
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param settlementTime The time (in years) to underlying swap settlement.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   * @deprecated Use the constructor that does not take a yield curve name
   */
  @Deprecated
  public CapFloorCMS(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional,
      final double fixingTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime,
      final double strike, final boolean isCap) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
    _strike = strike;
    _isCap = isCap;
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
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   */
  public CapFloorCMS(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional,
      final double fixingTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime,
      final double strike, final boolean isCap) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Cap/floor CMS builder from a CMS coupon, the strike and the cap/floor flag.
   * @param coupon The CMS coupon.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   * @return The CMS cap/floor.
   */
  @SuppressWarnings("deprecation")
  public static CapFloorCMS from(final CouponCMS coupon, final double strike, final boolean isCap) {
    try {
      return new CapFloorCMS(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFundingCurveName(),
          coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), coupon.getUnderlyingSwap(), coupon.getSettlementTime(), strike, isCap);
    } catch (final IllegalStateException e) {
      return new CapFloorCMS(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(),
          coupon.getFixingTime(), coupon.getUnderlyingSwap(), coupon.getSettlementTime(), strike, isCap);
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

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  @SuppressWarnings("deprecation")
  public Coupon withNotional(final double notional) {
    try {
      return new CapFloorCMS(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(),
          notional, getFixingTime(), _underlyingSwap, _settlementTime, _strike, _isCap);
    } catch (final IllegalStateException e) {
      return new CapFloorCMS(getCurrency(), getPaymentTime(), getPaymentYearFraction(),
          notional, getFixingTime(), _underlyingSwap, _settlementTime, _strike, _isCap);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSwap.hashCode();
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
    final CapFloorCMS other = (CapFloorCMS) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorCMS(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorCMS(this);
  }

}
