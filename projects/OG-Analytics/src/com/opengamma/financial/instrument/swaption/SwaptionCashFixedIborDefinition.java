/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a vanilla swap with cash delivery.
 */
public final class SwaptionCashFixedIborDefinition implements FixedIncomeInstrumentConverter<SwaptionCashFixedIbor> {

  /**
   * Swap underlying the swaption.
   */
  private final SwapFixedIborDefinition _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The cash settlement date of the swaption.
   */
  private final ZonedDateTime _settlementDate;
  private final Expiry _expiry;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flag.
   * @param expiryDate The expiry date.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionCashFixedIborDefinition(final ZonedDateTime expiryDate, final double strike, final SwapFixedIborDefinition underlyingSwap, final boolean isCall, final boolean isLong) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    //TODO do we need to check that the swaption expiry is consistent with the underlying swap?
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _settlementDate = underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate();
    _expiry = new Expiry(expiryDate);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionCashFixedIborDefinition from(final ZonedDateTime expiryDate, final SwapFixedIborDefinition underlyingSwap, final boolean isLong) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getRate();
    // Implementation note: cash-settle swaptions underlying have the same rate on all coupons and standard conventions.
    return new SwaptionCashFixedIborDefinition(expiryDate, strike, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  @Override
  public SwaptionCashFixedIbor toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double expiryTime = actAct.getDayCountFraction(date, _expiry.getExpiry());
    final double settlementTime = actAct.getDayCountFraction(date, _settlementDate);
    final FixedCouponSwap<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(date, yieldCurveNames);
    return SwaptionCashFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isLong);
  }

  /**
   * Gets the underlying swap field.
   * @return The underlying swap.
   */
  public SwapFixedIborDefinition getUnderlyingSwap() {
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

  public Expiry getExpiry() {
    return _expiry;
  }

  @Override
  public String toString() {
    String result = "European swaption cash delivery: \n";
    result += "Expiry date: " + _expiry.toString() + ", Long: " + _isLong;
    result += "\nUnderlying swap: \n" + _underlyingSwap.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiry.hashCode();
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
    return ObjectUtils.equals(_underlyingSwap, other._underlyingSwap);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwaptionCashFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwaptionCashFixedIborDefinition(this);
  }

}
