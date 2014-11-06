/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
/**
 * Generic index future definition. An IndexFuture is always cash-settled.
 */
public class IndexFutureDefinition implements InstrumentDefinitionWithData<IndexFuture, Double> {
  /** ZonedDateTime on which settlement value of index is fixed */
  private final ZonedDateTime _expiryDate;
  /** Date on which payment is made */
  private final ZonedDateTime _settlementDate;
  /** Identifier of the underlying commodity */
  private final ExternalId _underlying;
  /** reference price. Typically the price at which the trade was last margined */
  private final double _referencePrice;
  private final Currency _currency;
  /** Notional of a single contract */
  private final double _unitAmount;

  /**
   * Basic setup for an Equity Future. TODO resolve conventions; complete param set
   * @param expiryDate The date-time at which the reference rate is fixed and the future is cash settled
   * @param settlementDate The date on which exchange is made, whether physical asset or cash equivalent
   * @param strikePrice The reference price at which the future will be settled
   * @param currency The reporting currency of the future
   * @param unitValue The currency value that the price of one contract will move by when the asset's price moves by one point
   * @param underlying ExtenalId of the underlying index
   */
  public IndexFutureDefinition(
      final ZonedDateTime expiryDate,
      final ZonedDateTime settlementDate,
      final double strikePrice,
      final Currency currency,
      final double unitValue,
      final ExternalId underlying) {
    Validate.notNull(expiryDate, "expiry");
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(currency, "currency");
    _expiryDate = expiryDate;
    _settlementDate = settlementDate;
    _referencePrice = strikePrice;
    _currency = currency;
    _unitAmount = unitValue;
    _underlying = underlying;
  }

  /**
   * Gets the _expiryDate.
   * @return the _expiryDate
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the _settlementDate.
   * @return the _settlementDate
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the reference price.
   * @return referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Gets the reference price.
   * @return referencePrice
   */
  public double getStrikePrice() {
    return getReferencePrice();
  }
  
  /**
   * Gets the _currency.
   * @return the _currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the _unitAmount.
   * @return the _unitAmount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }
  
  /**
   * Gets the underlying.
   * @return the underlying
   */
  public ExternalId getUnderlying() {
    return _underlying;
  }
  
  /**
   * Gets the settlementType.
   * @return CASH
   */
  public SettlementType getSettlementType() {
    return SettlementType.CASH;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final IndexFutureDefinition other = (IndexFutureDefinition) obj;
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitIndexFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitIndexFutureDefinition(this);
  }

  @Override
  public IndexFuture toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexFuture toDerivative(ZonedDateTime date, Double referencePrice, String... yieldCurveNames) {
    return toDerivative(date, referencePrice);
  }

  @Override
  public IndexFuture toDerivative(ZonedDateTime date) {
    return toDerivative(date, getReferencePrice());
  }

  @Override
  public IndexFuture toDerivative(ZonedDateTime date, Double referencePrice) {
    ArgumentChecker.notNull(date, "date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    final IndexFuture newDeriv = new IndexFuture(timeToFixing, timeToDelivery, referencePrice, getCurrency(), getUnitAmount());
    return newDeriv;
  }


}
