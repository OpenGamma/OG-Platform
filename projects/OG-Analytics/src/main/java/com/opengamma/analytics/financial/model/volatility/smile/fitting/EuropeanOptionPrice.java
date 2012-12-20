/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple container for European option price date - contains the price, strike, expiry and whether the option is a call or put
 */
public class EuropeanOptionPrice {

  private final double _strike;
  private final double _expiry;
  private final boolean _isCall;
  private final double _price;

  public EuropeanOptionPrice(final double price, final double strike, final boolean isCall, final double expiry) {
    ArgumentChecker.isTrue(price >= 0, "price is negative {}", price);
    ArgumentChecker.isTrue(strike >= 0, "strike is negative {}", strike);
    ArgumentChecker.isTrue(expiry >= 0, "expiry is negative {}", expiry);
    _price = price;
    _expiry = expiry;
    _isCall = isCall;
    _strike = strike;
  }

  /**
   * Gets the price.
   * @return the price
   */
  public double getPrice() {
    return _price;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public double getExpiry() {
    return _expiry;
  }

  /**
   * Gets the isCall.
   * @return the isCall
   */
  public boolean isCall() {
    return _isCall;
  }

  public EuropeanOptionPrice withPrice(final double price) {
    return new EuropeanOptionPrice(price, _strike, _isCall, _expiry);
  }

  public EuropeanOptionPrice withStrike(final double strike) {
    return new EuropeanOptionPrice(_price, strike, _isCall, _expiry);
  }

  public EuropeanOptionPrice withExpiry(final double expiry) {
    return new EuropeanOptionPrice(_price, _strike, _isCall, expiry);
  }

  public EuropeanOptionPrice withIsCall(final boolean isCall) {
    return new EuropeanOptionPrice(_price, _strike, isCall, _expiry);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_strike);
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
    EuropeanOptionPrice other = (EuropeanOptionPrice) obj;
    if (Double.doubleToLongBits(_expiry) != Double.doubleToLongBits(other._expiry)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_price) != Double.doubleToLongBits(other._price)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
