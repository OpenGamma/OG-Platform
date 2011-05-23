/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * Class describing an amount of a currency.
 */
public class CurrencyAmount {

  /**
   * @param currency The currency, not null
   * @param amount The amount
   * @return The currency amount
   */
  public static CurrencyAmount of(final Currency currency, final double amount) {
    return new CurrencyAmount(currency, amount);
  }

  private final Currency _currency;
  private final double _amount;

  /**
   * @param currency The currency, not null
   * @param amount The amount
   */
  public CurrencyAmount(final Currency currency, final double amount) {
    Validate.notNull(currency, "currency");
    _currency = currency;
    _amount = amount;
  }

  /**
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return The amount
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Adds another CurrencyAmount to this one. Only allowed for CurrencyAmounts with the same currency.
   * @param other The CurrencyAmount to add, not null
   * @return A CurrencyAmount 
   * @throws IllegalArgumentException If the currencies are not equal
   */
  public CurrencyAmount add(final CurrencyAmount other) {
    Validate.notNull(other, "other CurrencyAmount was null");
    Validate.isTrue(other.getCurrency().equals(_currency), "Can only add two CurrencyAmounts with the same currency");
    return new CurrencyAmount(_currency, other.getAmount() + _amount);
  }

  /**
   * Scales the currency amount
   * @param scale The scale
   * @return A CurrencyAmount
   */
  public CurrencyAmount scale(final double scale) {
    return new CurrencyAmount(_currency, _amount * scale);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _currency.hashCode();
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
    final CurrencyAmount other = (CurrencyAmount) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    return ObjectUtils.equals(_currency, other._currency);
  }

}
