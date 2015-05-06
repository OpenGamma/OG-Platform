/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of transaction on an interest rate future option with up-front margin security.
 */
public class InterestRateFutureOptionPremiumTransaction extends FuturesTransaction<InterestRateFutureOptionPremiumSecurity> {

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
  public InterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumSecurity underlyingOption,
                                                    int quantity,
                                                    double premiumTime,
                                                    double tradePrice) {
    super(underlyingOption, quantity, tradePrice);
    final double premiumAmount = -tradePrice * quantity * underlyingOption.getUnderlyingFuture().getNotional() * underlyingOption.getUnderlyingFuture().getPaymentAccrualFactor();
    _premium = new PaymentFixed(underlyingOption.getCurrency(), premiumTime, premiumAmount);
  }

  /**
   * Gets the premium payment.
   * @return The premium.
   */
  public PaymentFixed getPremium() {
    return _premium;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    InterestRateFutureOptionPremiumTransaction that = (InterestRateFutureOptionPremiumTransaction) o;

    if (!_premium.equals(that._premium)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + _premium.hashCode();
    return result;
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
