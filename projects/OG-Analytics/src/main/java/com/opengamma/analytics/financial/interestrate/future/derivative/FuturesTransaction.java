/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Abstract class for generic futures transactions.
 * @param <F> The futures type of the underlying security.
 */
public abstract class FuturesTransaction<F extends FuturesSecurity> implements InstrumentDerivative {

  /**
   * The underlying future security. Not null.
   */
  private final F _underlyingFuture;
  /**
   * Quantity of future. Can be positive or negative.
   */
  private final long _quantity;
  /**
   * The reference price. It is the transaction price on the transaction date and the last close price afterward.
   */
  private final double _referencePrice;

  /**
   * Constructor.
   * @param underlyingFuture The underlying futures security.
   * @param quantity The transaction quantity.
   * @param referencePrice The reference price.
   */
  public FuturesTransaction(F underlyingFuture, long quantity, double referencePrice) {
    super();
    ArgumentChecker.notNull(underlyingFuture, "underlying futures security");
    _underlyingFuture = underlyingFuture;
    _quantity = quantity;
    _referencePrice = referencePrice;
  }

  /**
   * Returns the underlying futures security.
   * @return The futures.
   */
  public F getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Returns the transaction quantity.
   * @return The quantity.
   */
  public long getQuantity() {
    return _quantity;
  }

  /**
   * Returns the reference price.
   * @return The price.
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Returns the futures currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingFuture.getCurrency();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_quantity ^ (_quantity >>> 32));
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
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
    FuturesTransaction<?> other = (FuturesTransaction<?>) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}
