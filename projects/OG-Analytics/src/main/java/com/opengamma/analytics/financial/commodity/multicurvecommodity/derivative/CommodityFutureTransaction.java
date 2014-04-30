/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class CommodityFutureTransaction implements InstrumentDerivative {

  /**
   * The underlying STIR futures security.
   */
  private final CommodityFutureSecurity _underlying;
  /**
   * The reference price is used to express present value with respect to some level, for example, the transaction price on the transaction date or the last close price afterward.
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;

  public CommodityFutureTransaction(final CommodityFutureSecurity underlying, final int quantity, final double referencePrice) {
    ArgumentChecker.notNull(underlying, "Underlying futures");
    ArgumentChecker.notNull(referencePrice, "The reference price");
    ArgumentChecker.notNull(quantity, "Quantity");
    _underlying = underlying;
    _referencePrice = referencePrice;
    _quantity = quantity;
  }

  /**
   * @return the _underlying
   */
  public CommodityFutureSecurity getUnderlying() {
    return _underlying;
  }

  /**
   * @return the _underlying
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * @return the _referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * @return the _quantity
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the unit amount.
   * @return The unit amount.
   */
  public double getUnitAmount() {
    return _underlying.getUnitAmount();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
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
    final CommodityFutureTransaction other = (CommodityFutureTransaction) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
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
