/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

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
}
