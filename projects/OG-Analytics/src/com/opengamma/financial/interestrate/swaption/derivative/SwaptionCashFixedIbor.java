/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.money.Currency;

/**
 * Class describing a European swaption on a vanilla swap with cash delivery.
 */
public final class SwaptionCashFixedIbor extends EuropeanVanillaOption implements InstrumentDerivative {

  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The time (in years) to cash settlement.
   */
  private final double _settlementTime;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param settlementTime The time (in years) to cash settlement.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionCashFixedIbor(double expiryTime, double strike, FixedCouponSwap<? extends Payment> underlyingSwap, double settlementTime, boolean isCall, boolean isLong) {
    super(strike, expiryTime, isCall);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate() == strike, "Strike not in line with underlying");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
    _isLong = isLong;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param settlementTime The time (in years) to cash settlement.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionCashFixedIbor from(double expiryTime, FixedCouponSwap<? extends Payment> underlyingSwap, double settlementTime, boolean isLong) {
    Validate.notNull(underlyingSwap, "underlying swap");
    double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate();
    // Implementation note: cash-settle swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedIbor(expiryTime, strike, underlyingSwap, settlementTime, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong field.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the swaption settlement time (in years).
   * @return The settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the time difference between the last fixed leg payment and the settlement.
   * @return The maturity time.
   */
  public double getMaturityTime() {
    return _underlyingSwap.getFixedLeg().getNthPayment(_underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - _settlementTime;
  }

  /**
   * Gets the swaption currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingSwap.getFirstLeg().getCurrency();
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitSwaptionCashFixedIbor(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwaptionCashFixedIbor(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isLong ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
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
    SwaptionCashFixedIbor other = (SwaptionCashFixedIbor) obj;
    if (_isLong != other._isLong) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
