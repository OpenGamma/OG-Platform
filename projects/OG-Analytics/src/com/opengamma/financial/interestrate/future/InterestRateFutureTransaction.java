/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * Description of transaction on an interest rate future security.
 */
public class InterestRateFutureTransaction implements InterestRateDerivative {

  /**
   * The underlying future security.
   */
  private final InterestRateFutureSecurity _underlyingFuture;
  /**
   * The quantity of future. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The reference price. It is the transaction price on the transaction date and the last close price afterward.
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;

  /**
   * The future transaction constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of future. 
   * @param referencePrice The reference price.
   */
  public InterestRateFutureTransaction(InterestRateFutureSecurity underlyingFuture, int quantity, double referencePrice) {
    Validate.notNull(underlyingFuture, "underlying future");
    this._underlyingFuture = underlyingFuture;
    this._quantity = quantity;
    this._referencePrice = referencePrice;
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future security.
   */
  public InterestRateFutureSecurity getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the quantity of future. 
   * @return The quantity of future. 
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the reference price.
   * @return The reference price.
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitInterestRateFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitInterestRateFutureTransaction(this);
  }

  @Override
  public String toString() {
    String result = "IRFuture Transaction: ";
    result += " Underlying: " + _underlyingFuture.toString();
    result += " Reference price: " + _referencePrice;
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
    InterestRateFutureTransaction other = (InterestRateFutureTransaction) obj;
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
