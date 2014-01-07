/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExpiredException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future security.
 */
public class SwapFuturesPriceDeliverableTransactionDefinition implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * The underlying swap futures security.
   */
  private final SwapFuturesPriceDeliverableSecurityDefinition _underlying;
  /**
   * The date at which the transaction was done.
   */
  private final ZonedDateTime _transactionDate;
  /**
   * The price at which the transaction was done.
   */
  private final double _transactionPrice;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;

  /**
   * Constructor.
   * @param underlying The underlying futures.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param quantity The quantity/number of contract.
   */
  public SwapFuturesPriceDeliverableTransactionDefinition(final SwapFuturesPriceDeliverableSecurityDefinition underlying, final ZonedDateTime transactionDate,
      final double transactionPrice, final int quantity) {
    ArgumentChecker.notNull(underlying, "Underlying");
    ArgumentChecker.notNull(transactionDate, "Transaction date");
    _underlying = underlying;
    _transactionDate = transactionDate;
    _transactionPrice = transactionPrice;
    _quantity = quantity;
  }

  /**
   * Returns the underlying futures security.
   * @return The underlying.
   */
  public SwapFuturesPriceDeliverableSecurityDefinition getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the date at which the transaction was done.
   * @return The transaction date.
   */
  public ZonedDateTime getTransactionDate() {
    return _transactionDate;
  }

  /**
   * Gets the price at which the transaction was done.
   * @return The transaction price.
   */
  public double getTransactionPrice() {
    return _transactionPrice;
  }

  /**
   * Gets the quantity/number of contract.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final LocalDate date = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = _transactionDate.toLocalDate();
    final LocalDate deliveryDateLocal = _underlying.getDeliveryDate().toLocalDate();
    if (date.isAfter(deliveryDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last trading date, " + deliveryDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _transactionPrice;
    }
    final SwapFuturesPriceDeliverableSecurity underlying = _underlying.toDerivative(dateTime, yieldCurveNames);
    final SwapFuturesPriceDeliverableTransaction future = new SwapFuturesPriceDeliverableTransaction(underlying, referencePrice, _quantity);
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate date = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = _transactionDate.toLocalDate();
    final LocalDate deliveryDateLocal = _underlying.getDeliveryDate().toLocalDate();
    if (date.isAfter(deliveryDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last trading date, " + deliveryDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _transactionPrice;
    }
    final SwapFuturesPriceDeliverableSecurity underlying = _underlying.toDerivative(dateTime);
    final SwapFuturesPriceDeliverableTransaction future = new SwapFuturesPriceDeliverableTransaction(underlying, referencePrice, _quantity);
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the one argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this);
  }

  @Override
  public String toString() {
    final String result = "Quantity: " + _quantity + " of " + _underlying.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    result = prime * result + _transactionDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_transactionPrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlying.hashCode();
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
    final SwapFuturesPriceDeliverableTransactionDefinition other = (SwapFuturesPriceDeliverableTransactionDefinition) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_transactionDate, other._transactionDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_transactionPrice) != Double.doubleToLongBits(other._transactionPrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
