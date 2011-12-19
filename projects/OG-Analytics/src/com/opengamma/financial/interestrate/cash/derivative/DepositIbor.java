/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a deposit underlying a Ibor index. Used in particular for Ibor fixing and curve construction.
 */
public class DepositIbor extends Cash {

  /**
   * The Ibor-like index associated to the deposit.
   */
  private final IborIndex _index;

  /**
   * Constructor from all details.
   * @param currency The currency
   * @param startTime The deposit start time.
   * @param endTime The deposit end (or maturity) time.
   * @param notional The deposit notional.
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param rate The deposit rate.
   * @param accrualFactor The accrual factor (or year fraction).
   * @param index The associated index.
   * @param indexCurveName The name of the curve associated to the index.
   */
  public DepositIbor(final Currency currency, final double startTime, final double endTime, final double notional, final double initialAmount, final double rate, final double accrualFactor,
      final IborIndex index, final String indexCurveName) {
    super(currency, startTime, endTime, notional, initialAmount, rate, accrualFactor, indexCurveName);
    Validate.notNull(index, "Index");
    Validate.isTrue(currency.equals(index.getCurrency()), "Currency should be equal to index currency");
    _index = index;
  }

  /**
   * Gets the Ibor-like index associated to the deposit.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _index.hashCode();
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
    DepositIbor other = (DepositIbor) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitDepositIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitDepositIbor(this);
  }

}
