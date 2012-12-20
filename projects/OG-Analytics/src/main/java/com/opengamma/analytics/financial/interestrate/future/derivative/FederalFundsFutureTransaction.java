/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Description of an transaction on a Federal Funds Futures.
 */
public class FederalFundsFutureTransaction implements InstrumentDerivative {

  /**
   * The underlying future security.
   */
  private final FederalFundsFutureSecurity _underlyingFuture;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The reference price. It is the transaction price on the transaction date and the last close (margining) price afterward.
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param referencePrice The reference price. It is the transaction price on the transaction date and the last close (margining) price afterward.
   */
  public FederalFundsFutureTransaction(FederalFundsFutureSecurity underlyingFuture, int quantity, double referencePrice) {
    Validate.notNull(underlyingFuture, "Future");
    _underlyingFuture = underlyingFuture;
    _quantity = quantity;
    _referencePrice = referencePrice;
  }

  /**
   * Gets the underlying future security.
   * @return The future.
   */
  public FederalFundsFutureSecurity getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the quantity of the transaction. Can be positive or negative.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the reference price. It is the transaction price on the transaction date and the last close (margining) price afterward.
   * @return The reference price.
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFederalFundsFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFederalFundsFutureTransaction(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
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
    FederalFundsFutureTransaction other = (FederalFundsFutureTransaction) obj;
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
