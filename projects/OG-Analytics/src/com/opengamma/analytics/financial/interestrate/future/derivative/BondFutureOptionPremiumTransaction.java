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
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;

/**
 * Description of transaction on an bond future option with up-front margin security.
 */
public class BondFutureOptionPremiumTransaction implements InstrumentDerivative {

  /**
   * The underlying option future security.
   */
  private final BondFutureOptionPremiumSecurity _underlyingOption;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The premium payment. If the payment is in the past, the paymentTime is 0 and the amount 0.
   * If the payment is today or in the future, the premium amount is given by the the transaction price * future notional * future accrual factor.
   */
  private final PaymentFixed _premium;

  /**
  * Constructor of the future option transaction from details.
  * @param underlyingOption The underlying option future security.
  * @param quantity The quantity of the transaction. Can be positive or negative.
  * @param premium The transaction premium.
  */
  public BondFutureOptionPremiumTransaction(BondFutureOptionPremiumSecurity underlyingOption, int quantity, PaymentFixed premium) {
    Validate.notNull(underlyingOption, "underlying option");
    this._underlyingOption = underlyingOption;
    this._quantity = quantity;
    _premium = premium;
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying option.
   */
  public BondFutureOptionPremiumSecurity getUnderlyingOption() {
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
   * Gets the premium payment.
   * @return The premium.
   */
  public PaymentFixed getPremium() {
    return _premium;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _premium.hashCode();
    result = prime * result + _quantity;
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
    BondFutureOptionPremiumTransaction other = (BondFutureOptionPremiumTransaction) obj;
    if (!ObjectUtils.equals(_premium, other._premium)) {
      return false;
    }
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondFutureOptionPremiumTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFutureOptionPremiumTransaction(this);
  }

}
