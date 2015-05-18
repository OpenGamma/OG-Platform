/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of transaction on an bond future option with up-front margin security.
 */
public class BondFuturesOptionPremiumTransaction implements InstrumentDerivative {

  /**
   * The underlying option future security.
   */
  private final BondFuturesOptionPremiumSecurity _underlyingOption;
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
  public BondFuturesOptionPremiumTransaction(final BondFuturesOptionPremiumSecurity underlyingOption, final int quantity, final PaymentFixed premium) {
    ArgumentChecker.notNull(underlyingOption, "Underlying option");
    ArgumentChecker.notNull(premium, "Premium");
    _underlyingOption = underlyingOption;
    _quantity = quantity;
    _premium = premium;
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying option.
   */
  public BondFuturesOptionPremiumSecurity getUnderlyingOption() {
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
    result = prime * result + _premium.hashCode();
    result = prime * result + _quantity;
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
    final BondFuturesOptionPremiumTransaction other = (BondFuturesOptionPremiumTransaction) obj;
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
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureOptionPremiumTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFutureOptionPremiumTransaction(this);
  }

}
