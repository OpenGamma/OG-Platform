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
 * Abstract class for generic transaction on securities with a futures-style margining feature.
 * @param <F> The type of the underlying security.
 */
public abstract class FuturesTransaction<F extends FuturesSecurity> implements InstrumentDerivative {

  /**
   * The underlying future security. Not null.
   */
  private final F _underlyingSecurity;
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
   * @param underlyingSecurity The underlying futures security.
   * @param quantity The transaction quantity.
   * @param referencePrice The reference price.
   */
  public FuturesTransaction(F underlyingSecurity, long quantity, double referencePrice) {
    super();
    ArgumentChecker.notNull(underlyingSecurity, "underlying futures security");
    _underlyingSecurity = underlyingSecurity;
    _quantity = quantity;
    _referencePrice = referencePrice;
  }

  /**
   * Returns the underlying futures security.
   * @return The futures.
   */
  public F getUnderlyingSecurity() {
    return _underlyingSecurity;
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
    return _underlyingSecurity.getCurrency();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_quantity ^ (_quantity >>> 32));
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSecurity.hashCode();
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
    if (!ObjectUtils.equals(_underlyingSecurity, other._underlyingSecurity)) {
      return false;
    }
    return true;
  }

}
