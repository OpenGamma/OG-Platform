/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a European swaption on a vanilla swap with cash delivery.
 */
public final class SwaptionCashFixedCompoundedONCompounded extends EuropeanVanillaOption implements InstrumentDerivative {

  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final Swap<CouponFixedAccruedCompounding, CouponONCompounded> _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The time (in years) to cash settlement.
   */
  private final double _settlementTime;
  /**
   * The time in years to maturity.
   */
  private final double _maturityTime;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param settlementTime The time (in years) to cash settlement.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionCashFixedCompoundedONCompounded(final double expiryTime, final double strike, final Swap<CouponFixedAccruedCompounding, CouponONCompounded> underlyingSwap,
      final double settlementTime, final boolean isCall, final boolean isLong) {
    super(strike, expiryTime, isCall);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    _underlyingSwap = underlyingSwap;
    _settlementTime = settlementTime;
    _isLong = isLong;
    final Annuity<? extends Payment> firstLeg = underlyingSwap.getFirstLeg();
    _maturityTime = firstLeg.getNthPayment(firstLeg.getNumberOfPayments() - 1).getPaymentTime() - _settlementTime;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param settlementTime The time (in years) to cash settlement.
   * @param isLong The long (true) / short (false) flag.
   * @param strike The strike
   * @param isCall True if the option is a call
   * @return The swaption.
   */
  public static SwaptionCashFixedCompoundedONCompounded from(final double expiryTime, final Swap<CouponFixedAccruedCompounding, CouponONCompounded> underlyingSwap,
      final double settlementTime, final boolean isLong, final double strike, final boolean isCall) {
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    // Implementation note: cash-settle swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedCompoundedONCompounded(expiryTime, strike, underlyingSwap, settlementTime, isCall, isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public Swap<CouponFixedAccruedCompounding, CouponONCompounded> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the long / short flag.
   * @return True if the option is long
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
    return _maturityTime;
  }

  /**
   * Gets the swaption currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingSwap.getFirstLeg().getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionCashFixedCompoundedONCompounded(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionCashFixedCompoundedONCompounded(this);
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final SwaptionCashFixedCompoundedONCompounded other = (SwaptionCashFixedCompoundedONCompounded) obj;
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
