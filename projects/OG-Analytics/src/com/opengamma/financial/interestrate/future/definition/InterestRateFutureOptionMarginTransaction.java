/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Description of transaction on an interest rate future option with up-front margin security.
 */
public class InterestRateFutureOptionMarginTransaction implements InstrumentDerivative {

  /**
   * The underlying option future security.
   */
  private final InterestRateFutureOptionMarginSecurity _underlyingOption;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The reference price. It is the transaction price on the transaction date and the last close price afterward.
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;

  /**
  * Constructor of the future option transaction from details.
  * @param underlyingOption The underlying option future security.
  * @param quantity The quantity of the transaction. Can be positive or negative.
  * @param referencePrice The reference price.
  */
  public InterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginSecurity underlyingOption, int quantity, double referencePrice) {
    Validate.notNull(underlyingOption, "underlying option");
    this._underlyingOption = underlyingOption;
    this._quantity = quantity;
    this._referencePrice = referencePrice;
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying option.
   */
  public InterestRateFutureOptionMarginSecurity getUnderlyingOption() {
    return _underlyingOption;
  }

  /**
   * Gets the quantity of the transaction.
   * @return The quantity.
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingOption.hashCode();
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
    InterestRateFutureOptionMarginTransaction other = (InterestRateFutureOptionMarginTransaction) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitInterestRateFutureOptionMarginTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitInterestRateFutureOptionMarginTransaction(this);
  }

}
