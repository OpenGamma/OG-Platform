/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.SwaptionFixedIbor;

/**
 * Class describing a European swaption on a vanilla swap.
 */
public class SwaptionFixedIborDefinition implements FixedIncomeInstrumentDefinition<SwaptionFixedIbor> {
  /**
   * The expiry date (and time) of the swaption..
   */
  private final ZonedDateTime _expiryDate;
  /**
   * Swap underlying the swaption.
   */
  private final ZZZSwapFixedIborDefinition _underlyingSwap;
  //TODO: Should the cash/physical feature be in a flag or two instruments should be created?
  /**
   * A flag indicating if the swaption is cash-settled (true) or physical delivery (false).
   */
  private final boolean _isCash;

  public SwaptionFixedIborDefinition(ZonedDateTime expiryDate, ZZZSwapFixedIborDefinition underlyingSwap, boolean isCash) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    _expiryDate = expiryDate;
    _underlyingSwap = underlyingSwap;
    _isCash = isCash;
  }

  /**
   * Gets the _expiryDate field.
   * @return The expiry date.
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the _underlyingSwap field.
   * @return The underlying swap.
   */
  public ZZZSwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isCash field.
   * @return The cash(true)/physical(false) flag.
   */
  public boolean isCash() {
    return _isCash;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + (_isCash ? 1231 : 1237);
    result = prime * result + _underlyingSwap.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SwaptionFixedIborDefinition other = (SwaptionFixedIborDefinition) obj;
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

  @Override
  public SwaptionFixedIbor toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double expiryTime = actAct.getDayCountFraction(zonedDate, _expiryDate);
    final FixedCouponSwap<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(date, yieldCurveNames);
    return new SwaptionFixedIbor(expiryTime, underlyingSwap, _isCash);
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}
