/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
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
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param settlementTime The time (in years) to underlying swap settlement.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   */
  public CapFloorCMS(Currency currency, double paymentTime, double paymentYearFraction, double notional, double fixingTime, SwapFixedCoupon<? extends Payment> underlyingSwap, double settlementTime,
      double strike, boolean isCap) {
    super(currency, paymentTime, underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName(), paymentYearFraction, notional, fixingTime);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(underlyingSwap.isIborOrFixed(), "underlying swap not of vanilla type");
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
  public static CapFloorCMS from(CouponCMS coupon, double strike, boolean isCap) {
    return new CapFloorCMS(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), coupon.getUnderlyingSwap(),
        coupon.getSettlementTime(), strike, isCap);
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
  public double payOff(double fixing) {
    double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Coupon withNotional(double notional) {
    return null;
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
    CapFloorCMS other = (CapFloorCMS) obj;
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
