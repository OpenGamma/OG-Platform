/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of transaction on an bond future option security with premium paid up-front (CBOT type).
 */
public class BondFutureOptionPremiumTransactionDefinition implements InstrumentDefinition<BondFutureOptionPremiumTransaction> {

  /**
   * The underlying option future security.
   */
  private final BondFutureOptionPremiumSecurityDefinition _underlyingOption;
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
   * @param premiumAmount The transaction premium amount.
   */
  public BondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumSecurityDefinition underlyingOption, int quantity, ZonedDateTime premiumDate, double premiumAmount) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    ArgumentChecker.notNull(premiumDate, "premium date");
    ArgumentChecker.isTrue(premiumAmount * quantity <= 0, "Premium amount should have the opposite sign as quantity.");
    this._underlyingOption = underlyingOption;
    this._quantity = quantity;
    this._tradePrice = premiumAmount / (underlyingOption.getUnderlyingFuture().getNotional() * quantity);
    _premium = new PaymentFixedDefinition(underlyingOption.getCurrency(), premiumDate, premiumAmount);
  }

  /**
   * Builder of the future option transaction from the trade price.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param premiumDate The transaction date.
   * @param tradePrice The transaction price.
   * @return The option.
   */
  public static BondFutureOptionPremiumTransactionDefinition fromTradePrice(BondFutureOptionPremiumSecurityDefinition underlyingOption, int quantity, ZonedDateTime premiumDate, double tradePrice) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    double premiumAmount = tradePrice * underlyingOption.getUnderlyingFuture().getNotional() * quantity;
    return new BondFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, premiumDate, premiumAmount);
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying.
   */
  public BondFutureOptionPremiumSecurityDefinition getUnderlyingOption() {
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
   * The future option currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingOption.getCurrency();
  }

  @Override
  public BondFutureOptionPremiumTransaction toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(yieldCurveNames, "Curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "At least two curves required: credit and discounting");
    final BondFutureOptionPremiumSecurity option = _underlyingOption.toDerivative(date, yieldCurveNames);
    final double premiumTime = TimeCalculator.getTimeBetween(date, _premium.getPaymentDate());
    if (premiumTime < 0) { // Premium payment in the past: it is represented by a 0 payment today.
      return new BondFutureOptionPremiumTransaction(option, _quantity, new PaymentFixed(getCurrency(), 0, 0, yieldCurveNames[1]));
    }
    return new BondFutureOptionPremiumTransaction(option, _quantity, _premium.toDerivative(date, yieldCurveNames[1]));
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBondFutureOptionPremiumTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFutureOptionPremiumTransactionDefinition(this);
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
    BondFutureOptionPremiumTransactionDefinition other = (BondFutureOptionPremiumTransactionDefinition) obj;
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
