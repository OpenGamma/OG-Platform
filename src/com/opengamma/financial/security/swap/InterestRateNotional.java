/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.financial.Currency;


/**
 * Represents the notional value of an interest rate leg of a swap
 */
public class InterestRateNotional extends Notional {

  private Currency _currency;
  private double _amount;

  public InterestRateNotional(Currency currency, double amount) {
    _currency = currency;
    _amount = amount;
  }
  
  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  public boolean equals(final Object o) {
    if (!(o instanceof InterestRateNotional)) {
      return false;
    }
    final InterestRateNotional other = (InterestRateNotional) o;
    return ObjectUtils.equals(getCurrency(), other.getCurrency()) && (getAmount() == other.getAmount());
  }

  public int hashCode() {
    int hc = 1;
    hc = (hc * 17) + ObjectUtils.hashCode(getCurrency());
    hc = (hc * 17) + (int) getAmount();
    return hc;
  }

  public static InterestRateNotional fromFudgeMsg(final FudgeFieldContainer message) {
    final Currency currency = message.getFieldValue(Currency.class, message.getByName("currency"));
    final double amount = message.getFieldValue(Double.class, message.getByName("amount"));
    return new InterestRateNotional(currency, amount);
  }

}
