/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future security.
 */
public class SwapFuturesPriceDeliverableTransaction implements InstrumentDerivative {

  /**
   * The underlying swap futures security.
   */
  private final SwapFuturesPriceDeliverableSecurity _underlying;
  /**
   * The reference price is used to express present value with respect to some level, for example, the transaction price on the transaction date or the last close price afterward.  
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;

  public SwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableSecurity underlying, final double referencePrice, final int quantity) {
    ArgumentChecker.notNull(underlying, "Underlying futures");
    ArgumentChecker.notNull(referencePrice, "The reference price");
    ArgumentChecker.notNull(quantity, "Quantity");
    _underlying = underlying;
    _referencePrice = referencePrice;
    _quantity = quantity;
  }

  /**
   * Gets the future last trading time.
   * @return The future last trading time.
   */
  public SwapFuturesPriceDeliverableSecurity getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the referencePrice.
   * @return the referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
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

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesDeliverableTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesDeliverableTransaction(this);
  }

  @Override
  public String toString() {
    String result = "Quantity: " + _quantity + " of " + _underlying.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlying.hashCode();
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
    SwapFuturesPriceDeliverableTransaction other = (SwapFuturesPriceDeliverableTransaction) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
