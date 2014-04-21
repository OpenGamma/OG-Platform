/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a deposit to a specific counterpart. Used in particular for counterpart dependent valuation.
 */
public class DepositCounterpart extends Cash {

  /**
   * The counterpart name.
   */
  private final LegalEntity _counterparty;

  /**
   * Constructor from all details.
   * @param currency The currency
   * @param startTime The deposit start time.
   * @param endTime The deposit end (or maturity) time.
   * @param notional The deposit notional.
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param rate The deposit rate.
   * @param accrualFactor The accrual factor (or year fraction).
   * @param counterpartyName The counterpart name.
   * @param indexCurveName The name of the curve associated to the index.
   * @deprecated Use the constructor that does not take yield curve names
   */
  @Deprecated
  public DepositCounterpart(final Currency currency, final double startTime, final double endTime, final double notional, final double initialAmount, final double rate, final double accrualFactor,
      final String counterpartyName, final String indexCurveName) {
    super(currency, startTime, endTime, notional, initialAmount, rate, accrualFactor, indexCurveName);
    ArgumentChecker.notNull(counterpartyName, "Name");
    _counterparty = new LegalEntity(null, counterpartyName, null, null, null);
  }

  /**
   * Constructor from all details.
   * @param currency The currency
   * @param startTime The deposit start time.
   * @param endTime The deposit end (or maturity) time.
   * @param notional The deposit notional.
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param rate The deposit rate.
   * @param accrualFactor The accrual factor (or year fraction).
   * @param counterpartyName The counterpart name.
   */
  public DepositCounterpart(final Currency currency, final double startTime, final double endTime, final double notional, final double initialAmount, final double rate, final double accrualFactor,
      final String counterpartyName) {
    super(currency, startTime, endTime, notional, initialAmount, rate, accrualFactor);
    ArgumentChecker.notNull(counterpartyName, "Name");
    _counterparty = new LegalEntity(null, counterpartyName, null, null, null);
  }

  /**
   * Gets the counterpart name.
   * @return The name.
   */
  public String getCounterpartName() {
    return _counterparty.getShortName();
  }

  public LegalEntity getCounterparty() {
    return _counterparty;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _counterparty.hashCode();
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DepositCounterpart other = (DepositCounterpart) obj;
    if (!ObjectUtils.equals(_counterparty, other._counterparty)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositCounterpart(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositCounterpart(this);
  }

}
