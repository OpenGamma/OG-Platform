/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of transaction on an interest rate future option security with premium paid up-front (CME type).
 */
public class InterestRateFutureOptionPremiumTransactionDefinition implements InstrumentDefinition<InterestRateFutureOptionPremiumTransaction> {

  /**
   * The underlying option future security.
   */
  private final InterestRateFutureOptionPremiumSecurityDefinition _underlyingOption;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The transaction price. The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _tradePrice;
  /**
   * The premium payment: payment date and amount. The premium amount is given by the the transaction price * future notional * future accrual factor.
   */
  private final PaymentFixedDefinition _premium;

  /**
   * Constructor of the future option transaction from details.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param premiumDate The transaction date.
   * @param tradePrice The transaction price.
   */
  public InterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumSecurityDefinition underlyingOption, final int quantity,
      final ZonedDateTime premiumDate, final double tradePrice) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    ArgumentChecker.notNull(premiumDate, "premium date");
    _underlyingOption = underlyingOption;
    _quantity = quantity;
    _tradePrice = tradePrice;
    final double premiumAmount = _tradePrice * _underlyingOption.getUnderlyingFuture().getNotional() * _underlyingOption.getUnderlyingFuture().getPaymentAccrualFactor();
    _premium = new PaymentFixedDefinition(underlyingOption.getCurrency(), premiumDate, premiumAmount);
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying.
   */
  public InterestRateFutureOptionPremiumSecurityDefinition getUnderlyingOption() {
    return _underlyingOption;
  }

  /**
   * Gets the quantity of the transaction. Can be positive or negative.
   * @return The quantity of the transaction.
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
   * Gets the premium.
   * @return The premium.
   */
  public PaymentFixedDefinition getPremium() {
    return _premium;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureOptionPremiumTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public InterestRateFutureOptionPremiumTransaction toDerivative(final ZonedDateTime date) {
    final InterestRateFutureOptionPremiumSecurity option = _underlyingOption.toDerivative(date);
    final double premiumTime = TimeCalculator.getTimeBetween(date, _premium.getPaymentDate());
    if (premiumTime < 0) { // Premium payment in the past.
      // The premium payment is in the past and is represented by a 0 payment today.
      return new InterestRateFutureOptionPremiumTransaction(option, _quantity, 0.0, 0.0);
    }
    return new InterestRateFutureOptionPremiumTransaction(option, _quantity, premiumTime, _tradePrice);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionPremiumTransactionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _premium.hashCode();
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
    final InterestRateFutureOptionPremiumTransactionDefinition other = (InterestRateFutureOptionPremiumTransactionDefinition) obj;
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

}
