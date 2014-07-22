/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swaption;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a vanilla swap with physical delivery.
 */
public final class SwaptionPhysicalFixedIborDefinition implements InstrumentDefinition<SwaptionPhysicalFixedIbor> {

  /**
   * Swap underlying the swaption.
   */
  private final SwapFixedIborDefinition _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * Flag indicating if the option is a call (true) or put (false).
   */
  private final boolean _isCall;
  /**
   * The swaption expiry.
   */
  private final Expiry _expiry;
  /**
   * The currency.
   */
  private final Currency _currency;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flag.
   * @param expiryDate The expiry date.
   * @param strike The strike // TODO: remove
   * @param underlyingSwap The underlying swap.
   * @param isCall True if the swaption is a call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIborDefinition(final ZonedDateTime expiryDate, final double strike, final SwapFixedIborDefinition underlyingSwap, final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    _underlyingSwap = underlyingSwap;
    _currency = underlyingSwap.getCurrency();
    _isLong = isLong;
    _isCall = isCall;
    _expiry = new Expiry(expiryDate);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag. The strike stored in the EuropeanVanillaOptionDefinition should not be used for pricing as the
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   * @deprecated This relies on the {@link AnnuityDefinition#isPayer()} method to determine if the swaption is a call or a put, which is deprecated
   */
  @Deprecated
  public static SwaptionPhysicalFixedIborDefinition from(final ZonedDateTime expiryDate, final SwapFixedIborDefinition underlyingSwap, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIborDefinition(expiryDate, strike, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag. The strike stored in the EuropeanVanillaOptionDefinition should not be used for pricing as the
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isCall True if the swaption is a call (i.e. the underlying swap is a payer)
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIborDefinition from(final ZonedDateTime expiryDate, final SwapFixedIborDefinition underlyingSwap, final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIborDefinition(expiryDate, strike, underlyingSwap, isCall, isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the long / short flag.
   * @return True if the swaption is long.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the call / put flag.
   * @return True if the swaption is a call.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the expiry.
   * @return The expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * Gets the currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public String toString() {
    String result = "European swaption physical delivery: \n";
    result += "Expiry date: " + _expiry.toString() + ", Long: " + _isLong;
    result += "\nUnderlying swap: \n" + _underlyingSwap.toString();
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionPhysicalFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionPhysicalFixedIborDefinition(this);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwaptionPhysicalFixedIbor toDerivative(final ZonedDateTime dateTime, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(dateTime, yieldCurveNames);
    return SwaptionPhysicalFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isCall, _isLong);
  }

  @Override
  public SwaptionPhysicalFixedIbor toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(dateTime);
    return SwaptionPhysicalFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isCall, _isLong);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiry.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + _underlyingSwap.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SwaptionPhysicalFixedIborDefinition other = (SwaptionPhysicalFixedIborDefinition) obj;
    if (_isCall != other._isCall) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_expiry, other._expiry)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
