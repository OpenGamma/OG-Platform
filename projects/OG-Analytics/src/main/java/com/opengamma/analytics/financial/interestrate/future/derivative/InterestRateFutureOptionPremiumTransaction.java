/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of transaction on an interest rate future option with up-front margin security.
 */
public class InterestRateFutureOptionPremiumTransaction implements InstrumentDerivative {

  /**
   * The underlying option future security.
   */
  private final InterestRateFutureOptionPremiumSecurity _underlyingOption;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The transaction price. The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _tradePrice;
  /**
   * The premium payment. If the payment is in the past, the paymentTime is 0 and the amount 0.
   * If the payment is today or in the future, the premium amount is given by the the transaction price * future notional * future accrual factor.
   */
  private PaymentFixed _premium;

  /**
  * Constructor of the future option transaction from details.
  * @param underlyingOption The underlying option future security.
  * @param quantity The quantity of the transaction. Can be positive or negative.
  * @param premiumTime The transaction date.
  * @param tradePrice The transaction price.
  */
  @SuppressWarnings("deprecation")
  public InterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumSecurity underlyingOption, final int quantity, final double premiumTime, final double tradePrice) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    _underlyingOption = underlyingOption;
    _quantity = quantity;
    _tradePrice = tradePrice;
    final double premiumAmount = -_tradePrice * _quantity * _underlyingOption.getUnderlyingFuture().getNotional() * _underlyingOption.getUnderlyingFuture().getPaymentAccrualFactor();
    _premium = new PaymentFixed(underlyingOption.getCurrency(), premiumTime, premiumAmount);
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying option.
   */
  public InterestRateFutureOptionPremiumSecurity getUnderlyingOption() {
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
   * Gets the transaction price.
   * @return The transaction price.
   */
  public double getTradePrice() {
    return _tradePrice;
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
    result = prime * result + ((_premium == null) ? 0 : _premium.hashCode());
    result = prime * result + _quantity;
    long temp;
    temp = Double.doubleToLongBits(_tradePrice);
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
    final InterestRateFutureOptionPremiumTransaction other = (InterestRateFutureOptionPremiumTransaction) obj;
    if (!ObjectUtils.equals(_premium, other._premium)) {
      return false;
    }
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_tradePrice) != Double.doubleToLongBits(other._tradePrice)) {
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
    return visitor.visitInterestRateFutureOptionPremiumTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumTransaction(this);
  }

}
