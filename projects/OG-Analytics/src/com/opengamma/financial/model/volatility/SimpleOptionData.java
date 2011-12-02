/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import com.opengamma.lang.annotation.ExternalFunction;

/**
 * 
 */
public class SimpleOptionData {

  private final double _f;
  private final double _k;
  private final double _t;
  private final double _df;
  private final boolean _isCall;

  @ExternalFunction
  public SimpleOptionData(final double forward, final double strike, final double timeToExpiry, final double discountFactor, final boolean isCall) {
    _f = forward;
    _k = strike;
    _t = timeToExpiry;
    _df = discountFactor;
    _isCall = isCall;
  }

  /**
   * Gets the f.
   * @return the f
   */
  protected double getForward() {
    return _f;
  }

  /**
   * Gets the k.
   * @return the k
   */
  protected double getStrike() {
    return _k;
  }

  /**
   * Gets the t.
   * @return the t
   */
  protected double getTimeToExpiry() {
    return _t;
  }

  /**
   * Gets the df.
   * @return the df
   */
  protected double getDiscountFactor() {
    return _df;
  }

  /**
   * Gets the isCall.
   * @return the isCall
   */
  protected boolean isCall() {
    return _isCall;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_df);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_f);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_k);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_t);
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
    SimpleOptionData other = (SimpleOptionData) obj;
    if (Double.doubleToLongBits(_df) != Double.doubleToLongBits(other._df)) {
      return false;
    }
    if (Double.doubleToLongBits(_f) != Double.doubleToLongBits(other._f)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_k) != Double.doubleToLongBits(other._k)) {
      return false;
    }
    if (Double.doubleToLongBits(_t) != Double.doubleToLongBits(other._t)) {
      return false;
    }
    return true;
  }
}
