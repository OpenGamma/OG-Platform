/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a deposit to a specific counterpart. Used in particular for counterpart dependent valuation.
 */
public class DepositCounterpart extends Cash {

  /**
   * The counterpart name.
   */
  private final String _name;

  /**
   * Constructor from all details.
   * @param currency The currency
   * @param startTime The deposit start time.
   * @param endTime The deposit end (or maturity) time.
   * @param notional The deposit notional.
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param rate The deposit rate.
   * @param accrualFactor The accrual factor (or year fraction).
   * @param nameCounterpart The counterpart name.
   * @param indexCurveName The name of the curve associated to the index.
   */
  public DepositCounterpart(final Currency currency, final double startTime, final double endTime, final double notional, final double initialAmount, final double rate, final double accrualFactor,
      final String nameCounterpart, final String indexCurveName) {
    super(currency, startTime, endTime, notional, initialAmount, rate, accrualFactor, indexCurveName);
    Validate.notNull(nameCounterpart, "Name");
    _name = nameCounterpart;
  }

  /**
   * Gets the counterpart name.
   * @return The name.
   */
  public String getCounterpartName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _name.hashCode();
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
    DepositCounterpart other = (DepositCounterpart) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitDepositCounterpart(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitDepositCounterpart(this);
  }

}
