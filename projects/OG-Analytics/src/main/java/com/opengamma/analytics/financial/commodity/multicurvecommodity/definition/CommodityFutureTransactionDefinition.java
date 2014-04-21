/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.money.Currency;

/**
 * Abstract commodity future transaction definition.
 *
 * @param <T> concrete derivative class toDerivative() returns
 */
public abstract class CommodityFutureTransactionDefinition<T extends InstrumentDerivative> implements InstrumentDefinitionWithData<T, Double> {

  /**
   * The underlying futures security.
   */
  private final CommodityFutureSecurityDefinition<?> _underlying;
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
   * @param underlying The underlying future.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param quantity The quantity/number of contract.
   */
  public CommodityFutureTransactionDefinition(final CommodityFutureSecurityDefinition<?> underlying, final ZonedDateTime transactionDate, final double transactionPrice, final int quantity) {
    _underlying = underlying;
    _transactionDate = transactionDate;
    _transactionPrice = transactionPrice;
    _quantity = quantity;
  }

  /**
   * Gets the underlying future.
   * @return The underlying future.
   */
  public CommodityFutureSecurityDefinition<?> getUnderlying() {
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
   * Gets the future last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _underlying.getLastTradingDate();
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _underlying.getName();
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * Gets the quantity/number of contract.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  public abstract CommodityFutureTransactionDefinition<?> withNewTransactionPrice(final double transactionPrice);

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    result = prime * result + ((_transactionDate == null) ? 0 : _transactionDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_transactionPrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CommodityFutureTransactionDefinition<?> other = (CommodityFutureTransactionDefinition<?>) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (_transactionDate == null) {
      if (other._transactionDate != null) {
        return false;
      }
    } else if (!_transactionDate.equals(other._transactionDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_transactionPrice) != Double.doubleToLongBits(other._transactionPrice)) {
      return false;
    }
    if (_underlying == null) {
      if (other._underlying != null) {
        return false;
      }
    } else if (!_underlying.equals(other._underlying)) {
      return false;
    }
    return true;
  }

}
