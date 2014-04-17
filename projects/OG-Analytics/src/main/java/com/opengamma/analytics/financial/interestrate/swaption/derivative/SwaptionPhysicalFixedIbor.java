/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a European swaption on a vanilla swap.
 */
public final class SwaptionPhysicalFixedIbor extends EuropeanVanillaOption implements InstrumentDerivative {

  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final SwapFixedCoupon<? extends Payment> _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The time (in years) to swap settlement.
   */
  private final double _settlementTime;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param settlementTime Time to swap settlement.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIbor(final double expiryTime, final double strike, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime, final boolean isCall,
      final boolean isLong) {
    super(strike, expiryTime, isCall);
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _settlementTime = settlementTime;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag. The strike stored in the EuropeanVanillaOption should not be used for pricing as the
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param settlementTime Time to swap settlement.
   * @param isCall The call / put flag
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIbor from(final double expiryTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime,
      final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIbor(expiryTime, strike, underlyingSwap, settlementTime, isCall, isLong);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag. The strike stored in the EuropeanVanillaOption should not be used for pricing as the
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param settlementTime Time to swap settlement.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   * @deprecated This relies on the {@link AnnuityCouponFixed#isPayer()} method to determine if the swaption is a call or a put, which is deprecated
   */
  @Deprecated
  public static SwaptionPhysicalFixedIbor from(final double expiryTime, final SwapFixedCoupon<? extends Payment> underlyingSwap, final double settlementTime, final boolean isLong) {
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIbor(expiryTime, strike, underlyingSwap, settlementTime, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedCoupon<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong flag.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the settlement time.
   * @return The settlement time
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
  public String toString() {
    return "Swaption: Expiry=" + getTimeToExpiry() + ", is long=" + _isLong + "\n" + _underlyingSwap;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionPhysicalFixedIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionPhysicalFixedIbor(this);
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
    final SwaptionPhysicalFixedIbor other = (SwaptionPhysicalFixedIbor) obj;
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
