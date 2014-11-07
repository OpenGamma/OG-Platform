/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swaption;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a vanilla swap with cash delivery.
 */
public final class SwaptionCashFixedCompoundedONCompoundingDefinition implements InstrumentDefinitionWithData<SwaptionCashFixedCompoundedONCompounded, ZonedDateTimeDoubleTimeSeries> {

  /**
   * Swap underlying the swaption.
   */
  private final SwapFixedCompoundedONCompoundedDefinition _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The cash settlement date of the swaption.
   */
  private final ZonedDateTime _settlementDate;
  /**
   * The swaption expiry.
   */
  private final Expiry _expiry;
  /**
   * The currency.
   */
  private final Currency _currency;
  /**
   * The strike
   */
  private final double _strike;
  /**
   * The call / put flag.
   */
  private final boolean _isCall;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flag.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isCall True if the swaption is a call
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionCashFixedCompoundedONCompoundingDefinition(final ZonedDateTime expiryDate, final SwapFixedCompoundedONCompoundedDefinition underlyingSwap,
      final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    //TODO do we need to check that the swaption expiry is consistent with the underlying swap?
    final CouponFixedAccruedCompoundingDefinition firstPayment = underlyingSwap.getFixedLeg().getNthPayment(0);
    _underlyingSwap = underlyingSwap;
    _currency = underlyingSwap.getCurrency();
    _isLong = isLong;
    _settlementDate = underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate();
    _expiry = new Expiry(expiryDate);
    _strike = firstPayment.getRate();
    _isCall = isCall;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   * @deprecated This relies on the {@link AnnuityDefinition#isPayer()} method to determine if the swaption is a call or a put, which is deprecated
   */
  @Deprecated
  public static SwaptionCashFixedCompoundedONCompoundingDefinition from(final ZonedDateTime expiryDate, final SwapFixedCompoundedONCompoundedDefinition underlyingSwap, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    // Implementation note: cash-settled swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedCompoundedONCompoundingDefinition(expiryDate, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Builder from the expiry date, the underlying swap, a call/put flag and the long/short flag.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isCall True if the swaption is a call (i.e. the underlying swap is a payer)
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionCashFixedCompoundedONCompoundingDefinition from(final ZonedDateTime expiryDate, final SwapFixedCompoundedONCompoundedDefinition underlyingSwap,
      final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    // Implementation note: cash-settled swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedCompoundedONCompoundingDefinition(expiryDate, underlyingSwap, isCall, isLong);
  }

  @Override
  public SwaptionCashFixedCompoundedONCompounded toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _settlementDate);
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> underlyingSwap = (Swap<CouponFixedAccruedCompounding, CouponONCompounded>) _underlyingSwap.toDerivative(dateTime);
    return SwaptionCashFixedCompoundedONCompounded.from(expiryTime, underlyingSwap, settlementTime, _isLong, _strike, _isCall);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwaptionCashFixedCompoundedONCompounded toDerivative(final ZonedDateTime dateTime, final ZonedDateTimeDoubleTimeSeries ts, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SwaptionCashFixedCompoundedONCompounded toDerivative(final ZonedDateTime dateTime, final ZonedDateTimeDoubleTimeSeries ts) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _settlementDate);
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> underlyingSwap = (Swap<CouponFixedAccruedCompounding, CouponONCompounded>)
        _underlyingSwap.toDerivative(dateTime, new ZonedDateTimeDoubleTimeSeries[] {ts});
    return SwaptionCashFixedCompoundedONCompounded.from(expiryTime, underlyingSwap, settlementTime, _isLong, _strike, _isCall);
  }

  /**
   * Gets the underlying swap field.
   * @return The underlying swap.
   */
  public SwapDefinition getUnderlyingSwap() {
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
   * Gets the call / put flag.
   * @return True if the option is a call
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the swaption settlement date.
   * @return The settlement date.
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the swaption expiry date.
   * @return The expiry date.
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
    String result = "European swaption cash delivery: \n";
    result += "Expiry date: " + _expiry.toString() + ", Long: " + _isLong;
    result += "\nUnderlying swap: \n" + _underlyingSwap.toString();
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionCashFixedONCompoundingDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionCashFixedONCompoundingDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiry.hashCode();
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + (_isCall ? 1231 : 1237);
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
    final SwaptionCashFixedCompoundedONCompoundingDefinition other = (SwaptionCashFixedCompoundedONCompoundingDefinition) obj;
    if (!ObjectUtils.equals(_expiry, other._expiry)) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
