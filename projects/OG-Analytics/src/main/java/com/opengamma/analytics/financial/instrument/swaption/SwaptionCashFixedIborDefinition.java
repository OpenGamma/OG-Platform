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
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a fixed / float swap with cash delivery.
 */
public final class SwaptionCashFixedIborDefinition implements InstrumentDefinition<SwaptionCashFixedIbor> {

  /**
   * Swap underlying the swaption.
   */
  private final SwapDefinition _underlyingSwap;
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
   * Constructor from the expiry date, the underlying swap and the long/short flag.
   * @param expiryDate The expiry date.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionCashFixedIborDefinition(final ZonedDateTime expiryDate, final double strike, final SwapDefinition underlyingSwap, final boolean isCall, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = underlyingSwap.getFirstLeg();
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = underlyingSwap.getSecondLeg();
    boolean isPayer;
    if (payLeg instanceof AnnuityCouponFixedDefinition) {
      final AnnuityCouponFixedDefinition fixedLeg = (AnnuityCouponFixedDefinition) payLeg;
      _settlementDate = fixedLeg.getNthPayment(0).getAccrualStartDate();
      _currency = fixedLeg.getCurrency();
      isPayer = true;
    } else if (payLeg instanceof AnnuityCouponFixedAccruedCompoundingDefinition) {
      final AnnuityCouponFixedAccruedCompoundingDefinition fixedLeg = (AnnuityCouponFixedAccruedCompoundingDefinition) payLeg;
      _settlementDate = fixedLeg.getNthPayment(0).getAccrualStartDate();
      _currency = fixedLeg.getCurrency();
      isPayer = true;
    } else if (receiveLeg instanceof AnnuityCouponFixedDefinition) {
      final AnnuityCouponFixedDefinition fixedLeg = (AnnuityCouponFixedDefinition) receiveLeg;
      _settlementDate = fixedLeg.getNthPayment(0).getAccrualStartDate();
      _currency = fixedLeg.getCurrency();
      isPayer = false;
    } else if (receiveLeg instanceof AnnuityCouponFixedAccruedCompoundingDefinition) {
      final AnnuityCouponFixedAccruedCompoundingDefinition fixedLeg = (AnnuityCouponFixedAccruedCompoundingDefinition) receiveLeg;
      _settlementDate = fixedLeg.getNthPayment(0).getAccrualStartDate();
      _currency = fixedLeg.getCurrency();
      isPayer = false;
    } else {
      throw new IllegalArgumentException("Swap must have one leg that is an fixed");
    }
    ArgumentChecker.isTrue(isCall == isPayer, "Call flag not in line with underlying");
    //TODO do we need to check that the swaption expiry is consistent with the underlying swap?
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _expiry = new Expiry(expiryDate);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionCashFixedIborDefinition from(final ZonedDateTime expiryDate, final SwapDefinition underlyingSwap, final boolean isLong) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    final AnnuityDefinition<? extends PaymentDefinition> payLeg = underlyingSwap.getFirstLeg();
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = underlyingSwap.getSecondLeg();
    boolean isPayer;
    double strike;
    if (payLeg instanceof AnnuityCouponFixedDefinition) {
      strike = ((AnnuityCouponFixedDefinition) payLeg).getNthPayment(0).getRate();
      isPayer = true;
    } else if (payLeg instanceof AnnuityCouponFixedAccruedCompoundingDefinition) {
      strike = ((AnnuityCouponFixedAccruedCompoundingDefinition) payLeg).getNthPayment(0).getRate();
      isPayer = true;
    } else if (receiveLeg instanceof AnnuityCouponFixedDefinition) {
      strike = ((AnnuityCouponFixedDefinition) receiveLeg).getNthPayment(0).getRate();
      isPayer = false;
    } else if (receiveLeg instanceof AnnuityCouponFixedAccruedCompoundingDefinition) {
      strike = ((AnnuityCouponFixedAccruedCompoundingDefinition) receiveLeg).getNthPayment(0).getRate();
      isPayer = false;
    } else {
      throw new IllegalArgumentException("Swap must have one leg that is fixed");
    }
    // Implementation note: cash-settle swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedIborDefinition(expiryDate, strike, underlyingSwap, isPayer, isLong);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwaptionCashFixedIbor toDerivative(final ZonedDateTime dateTime, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _settlementDate);
    final SwapFixedCoupon<? extends Payment> underlyingSwap = (SwapFixedCoupon<? extends Payment>) _underlyingSwap.toDerivative(dateTime, yieldCurveNames);
    return SwaptionCashFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isLong);
  }

  @Override
  public SwaptionCashFixedIbor toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getExpiry().getExpiry().toLocalDate()), "date is after expiry date");
    final double expiryTime = TimeCalculator.getTimeBetween(dateTime, _expiry.getExpiry());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _settlementDate);
    final SwapFixedCoupon<? extends Payment> underlyingSwap = (SwapFixedCoupon<? extends Payment>) _underlyingSwap.toDerivative(dateTime);
    return SwaptionCashFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isLong);
  }


  /**
   * Gets the underlying swap field.
   * @return The underlying swap.
   */
  public SwapDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the isLong flag.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
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
   * @return The currency.
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
    return visitor.visitSwaptionCashFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionCashFixedIborDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiry.hashCode();
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
    final SwaptionCashFixedIborDefinition other = (SwaptionCashFixedIborDefinition) obj;
    if (!ObjectUtils.equals(_expiry, other._expiry)) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
