/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class CashSettledFuture implements InstrumentDerivative, Serializable {
  private final double _timeToExpiry;
  private final double _timeToSettlement;
  private final double _referencePrice;
  private final double _unitAmount;
  private final Currency _currency;

  /**
   * 
   * @param timeToExpiry    time (in years as a double) until the date-time at which the reference index is fixed
   * @param timeToSettlement  time (in years as a double) until the date-time at which the contract is settled
   * @param strike         Set strike price at trade time. Note that we may handle margin by resetting this at the end of each trading day
   * @param currency       The reporting currency of the future
   *  @param unitAmount    The unit value per tick, in given currency
   */
  public CashSettledFuture(final double timeToExpiry,
      final double timeToSettlement,
      final double strike,
      final Currency currency,
      final double unitAmount) {
    Validate.isTrue(unitAmount > 0, "point value must be positive");
    _timeToExpiry = timeToExpiry;
    _timeToSettlement = timeToSettlement;
    _referencePrice = strike;
    _unitAmount = unitAmount;
    _currency = currency;
  }

  /**
   * Gets the date when the reference rate is set
   * @return the fixing date (in years as a double)
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Gets the date when payments are made
   * @return the delivery date (in years as a double)
   */
  public double getTimeToSettlement() {
    return _timeToSettlement;
  }

  /**
   * The strike here is a reference price, generally the price at which the trade was last margined.
   * TODO Add margin accounting. Revise this doc when complete.
   * @return the strike
   */
  public double getStrike() {
    return _referencePrice;
  }

  /**
   * The strike here is a reference price, generally the price at which the trade was last margined.
   * TODO Add margin accounting. Revise this doc when complete.
   * @return the strike
   */
  public double getReferencePrice() {
    return _referencePrice;
  }
  
  /**
   * Gets the point value.
   * @return the point value
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Gets the currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCashSettledFuture(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCashSettledFuture(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_timeToSettlement);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_timeToExpiry);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final CashSettledFuture other = (CashSettledFuture) obj;

    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }

    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToSettlement) != Double.doubleToLongBits(other._timeToSettlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    return true;

  }
}
