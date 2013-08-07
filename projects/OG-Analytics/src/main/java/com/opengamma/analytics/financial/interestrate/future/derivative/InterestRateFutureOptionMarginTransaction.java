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
  public InterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginSecurity underlyingOption, final int quantity, final double referencePrice) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    _underlyingOption = underlyingOption;
    _quantity = quantity;
    _referencePrice = referencePrice;
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

  /**
   * Returns the transaction currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingOption.getCurrency();
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
    final InterestRateFutureOptionMarginTransaction other = (InterestRateFutureOptionMarginTransaction) obj;
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
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransaction(this);
  }

}
