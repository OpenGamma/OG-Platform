/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumTransaction;

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
  public InterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumSecurityDefinition underlyingOption, int quantity, ZonedDateTime premiumDate, double tradePrice) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(premiumDate, "premium date");
    this._underlyingOption = underlyingOption;
    this._quantity = quantity;
    this._tradePrice = tradePrice;
    double premiumAmount = _tradePrice * _underlyingOption.getUnderlyingFuture().getNotional() * _underlyingOption.getUnderlyingFuture().getPaymentAccrualFactor();
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

  @Override
  public InterestRateFutureOptionPremiumTransaction toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    final InterestRateFutureOptionPremiumSecurity option = _underlyingOption.toDerivative(date, yieldCurveNames);
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double premiumTime = actAct.getDayCountFraction(date, _premium.getPaymentDate());
    return new InterestRateFutureOptionPremiumTransaction(option, _quantity, premiumTime, _tradePrice);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitInterestRateFutureOptionPremiumTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
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
    InterestRateFutureOptionPremiumTransactionDefinition other = (InterestRateFutureOptionPremiumTransactionDefinition) obj;
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
