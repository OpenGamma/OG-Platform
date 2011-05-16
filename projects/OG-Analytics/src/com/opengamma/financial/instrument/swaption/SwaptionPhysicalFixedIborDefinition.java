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
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a vanilla swap with physical delivery.
 */
public final class SwaptionPhysicalFixedIborDefinition implements FixedIncomeInstrumentConverter<SwaptionPhysicalFixedIbor> {

  /**
   * Swap underlying the swaption.
   */
  private final SwapFixedIborDefinition _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  private final Expiry _expiry;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryDate The expiry date.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIborDefinition(final ZonedDateTime expiryDate, final double strike, final SwapFixedIborDefinition underlyingSwap, final boolean isCall, final boolean isLong) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _expiry = new Expiry(expiryDate);
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg. The strike stored in the EuropeanVanillaOptionDefinition should not be used for pricing as the 
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIborDefinition from(final ZonedDateTime expiryDate, final SwapFixedIborDefinition underlyingSwap, final boolean isLong) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    final double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIborDefinition(expiryDate, strike, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong flag.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  @Override
  public String toString() {
    String result = "European swaption physical delivery: \n";
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
    final SwaptionPhysicalFixedIborDefinition other = (SwaptionPhysicalFixedIborDefinition) obj;
    if (!ObjectUtils.equals(_expiry, other._expiry)) {
      return false;
    }
    return ObjectUtils.equals(_underlyingSwap, other._underlyingSwap);
  }

  @Override
  public SwaptionPhysicalFixedIbor toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    //final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double expiryTime = actAct.getDayCountFraction(date, _expiry.getExpiry());
    final double settlementTime = actAct.getDayCountFraction(date, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final FixedCouponSwap<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(date, yieldCurveNames);
    return SwaptionPhysicalFixedIbor.from(expiryTime, underlyingSwap, settlementTime, _isLong);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return null;
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}
