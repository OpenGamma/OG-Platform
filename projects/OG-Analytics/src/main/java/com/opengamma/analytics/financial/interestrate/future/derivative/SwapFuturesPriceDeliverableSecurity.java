/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of Deliverable Interest Rate Swap Futures as traded on CME.
 */
public class SwapFuturesPriceDeliverableSecurity extends FuturesSecurity {

  /**
   * The delivery time. Usually the third Wednesday of the month is the spot date.
   */
  private final double _deliveryTime;
  /**
   * The futures underlying swap. The delivery date should be the first accrual date of the underlying swap. The swap should be a receiver swap of notional 1.
   */
  private final SwapFixedCoupon<? extends Coupon> _underlyingSwap;
  /**
   * The notional of the future (also called face value or contract value).
   */
  private final double _notional;

  /**
   * Constructor.
   * @param lastTradingTime The futures last trading time.
   * @param deliveryTime The delivery time.
   * @param underlyingSwap The futures underlying swap.
   * @param notional The notional of the future (also called face value or contract value).
   */
  public SwapFuturesPriceDeliverableSecurity(final double lastTradingTime, final double deliveryTime, final SwapFixedCoupon<? extends Coupon> underlyingSwap, final double notional) {
    super(lastTradingTime);
    ArgumentChecker.notNull(underlyingSwap, "Underlying swap");
    _deliveryTime = deliveryTime;
    _underlyingSwap = underlyingSwap;
    _notional = notional;
  }

  /**
   * Gets the delivery time.
   * @return The time.
   */
  public double getDeliveryTime() {
    return _deliveryTime;
  }

  /**
   * Gets the futures underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedCoupon<? extends Coupon> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the notional of the future (also called face value or contract value).
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the futures currency.
   * @return The currency.
   */
  @Override
  public Currency getCurrency() {
    return _underlyingSwap.getFirstLeg().getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesPriceDeliverableSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesPriceDeliverableSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_deliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
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
    SwapFuturesPriceDeliverableSecurity other = (SwapFuturesPriceDeliverableSecurity) obj;
    if (Double.doubleToLongBits(_deliveryTime) != Double.doubleToLongBits(other._deliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
