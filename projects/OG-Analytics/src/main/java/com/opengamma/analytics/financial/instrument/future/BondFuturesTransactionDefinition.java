/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a bond future transaction (definition version).
 */
public class BondFuturesTransactionDefinition implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * Underlying future security.
   */
  private final BondFuturesSecurityDefinition _underlyingFuture;
  /**
   * Quantity of future. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * Transaction date.
   */
  private final ZonedDateTime _tradeDate;
  /**
   * Transaction price. The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _tradePrice;

  /**
   * Constructor of the future transaction.
   * @param underlyingFuture Underlying future security.
   * @param quantity Quantity of future. Can be positive or negative.
   * @param tradeDate Transaction date.
   * @param tradePrice Transaction price.
   */
  public BondFuturesTransactionDefinition(final BondFuturesSecurityDefinition underlyingFuture, final int quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    ArgumentChecker.notNull(underlyingFuture, "Underlying future");
    ArgumentChecker.notNull(tradeDate, "Trade date");
    _underlyingFuture = underlyingFuture;
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradePrice = tradePrice;
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future.
   */
  public BondFuturesSecurityDefinition getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the quantity of future. Can be positive or negative.
   * @return The quantity of future.
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the transaction date.
   * @return The transaction date.
   */
  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  /**
   * Gets the transaction price.
   * @return The transaction price.
   */
  public double getTradePrice() {
    return _tradePrice;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of BondFutureTransactionDefinition does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getUnderlyingFuture().getDeliveryLastDate()), "Date is after last delivery date");
    ArgumentChecker.isTrue(!date.isBefore(_tradeDate), "Date is before trade date");
    final BondFuturesSecurity underlyingFuture = _underlyingFuture.toDerivative(date, yieldCurveNames);
    double referencePrice;
    if (_tradeDate.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _tradePrice;
    }
    final BondFuturesTransaction futureTransaction = new BondFuturesTransaction(underlyingFuture, _quantity, referencePrice);
    return futureTransaction;
  }

  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of BondFutureTransactionDefinition does not support the two argument method (without margin price data).");
  }


  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date, final Double lastMarginPrice) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getUnderlyingFuture().getDeliveryLastDate()), "Date is after last delivery date");
    ArgumentChecker.isTrue(!date.isBefore(_tradeDate), "Date is before trade date");
    final BondFuturesSecurity underlyingFuture = _underlyingFuture.toDerivative(date);
    double referencePrice;
    if (_tradeDate.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _tradePrice;
    }
    final BondFuturesTransaction futureTransaction = new BondFuturesTransaction(underlyingFuture, _quantity, referencePrice);
    return futureTransaction;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFuturesTransactionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    result = prime * result + _tradeDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_tradePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
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
    final BondFuturesTransactionDefinition other = (BondFuturesTransactionDefinition) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_tradeDate, other._tradeDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradePrice) != Double.doubleToLongBits(other._tradePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}
