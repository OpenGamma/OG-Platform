/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.ForexDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a foreign exchange non-deliverable forward transaction.
 * The transaction is XXX/YYY where YYY is the currency for the cash-settlement. A NDF KRW/USD with USD cash settlement is stored with KRW as currency1 and USD as currency2.
 */
public class ForexNonDeliverableForward implements ForexDerivative {

  /**
   * First currency of the transaction.
   */
  private final Currency _currency1;
  /**
   * Second currency of the transaction. The cash settlement is done in this currency.
   */
  private final Currency _currency2;
  /**
   * Notional of the transaction (in currency2).
   */
  private final double _notional;
  /**
   * The reference exchange rate for the settlement (1 currency2 = _rate currency1).
   */
  private final double _exchangeRate;
  /**
   * The exchange rate fixing time.
   */
  private final double _fixingTime;
  /**
   * The transaction payment or settlement time.
   */
  private final double _paymentTime;
  /**
   * The discounting curve name used for currency1.
   */
  private final String _discountingCurve1Name;
  /**
   * The discounting curve name used for currency2.
   */
  private final String _discountingCurve2Name;

  /**
   * Constructor for non-deliverable forward Forex transaction.
   * @param currency1 First currency of the transaction.
   * @param currency2 Second currency of the transaction. The cash settlement is done in this currency.
   * @param notional Notional of the transaction (in currency2).
   * @param exchangeRate The reference exchange rate for the settlement (1 currency2 = _rate currency1).
   * @param fixingTime The exchange rate fixing time.
   * @param paymentTime The transaction payment or settlement time.
   * @param dsc1 The discounting curve name used for currency1.
   * @param dsc2 The discounting curve name used for currency2.
   */
  public ForexNonDeliverableForward(Currency currency1, Currency currency2, double notional, double exchangeRate, double fixingTime, double paymentTime, String dsc1, String dsc2) {
    Validate.notNull(currency1, "First currency");
    Validate.notNull(currency2, "Second currency");
    Validate.isTrue(currency1 != currency2, "Currencies should be different");
    Validate.isTrue(fixingTime <= paymentTime, "Payment time should be on or after fixing time");
    this._currency1 = currency1;
    this._currency2 = currency2;
    this._notional = notional;
    this._exchangeRate = exchangeRate;
    this._fixingTime = fixingTime;
    this._paymentTime = paymentTime;
    _discountingCurve1Name = dsc1;
    _discountingCurve2Name = dsc2;
  }

  /**
   * Gets the first currency of the transaction.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _currency1;
  }

  /**
   * Gets the second currency of the transaction. The cash settlement is done in this currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _currency2;
  }

  /**
   * Gets the notional of the transaction (in currency2).
   * @return The notional.
   */
  public double getNotionalCurrency2() {
    return _notional;
  }

  /**
   * Gets the notional of the transaction (in currency1).
   * @return The notional.
   */
  public double getNotionalCurrency1() {
    return _notional * _exchangeRate;
  }

  /**
   * Gets the reference exchange rate for the settlement.
   * @return The rate.
   */
  public double getExchangeRate() {
    return _exchangeRate;
  }

  /**
   * Gets The exchange rate fixing time.
   * @return The date.
   */
  public double getFixingTime() {
    return _fixingTime;
  }

  /**
   * Gets The transaction payment (or settlement) time.
   * @return The date.
   */
  public double getPaymentTime() {
    return _paymentTime;
  }

  /**
   * Gets the discounting curve name used for currency1.
   * @return The name.
   */
  public String getDiscountingCurve1Name() {
    return _discountingCurve1Name;
  }

  /**
   * Gets the discounting curve name used for currency2.
   * @return The name.
   */
  public String getDiscountingCurve2Name() {
    return _discountingCurve2Name;
  }

  @Override
  public <S, T> T accept(ForexDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexNonDeliverableForward(this, data);
  }

  @Override
  public <T> T accept(ForexDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexNonDeliverableForward(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency1.hashCode();
    result = prime * result + _currency2.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_exchangeRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    ForexNonDeliverableForward other = (ForexNonDeliverableForward) obj;
    if (!ObjectUtils.equals(_currency1, other._currency1)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency2, other._currency2)) {
      return false;
    }
    if (Double.doubleToLongBits(_exchangeRate) != Double.doubleToLongBits(other._exchangeRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingTime) != Double.doubleToLongBits(other._fixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    return true;
  }

}
