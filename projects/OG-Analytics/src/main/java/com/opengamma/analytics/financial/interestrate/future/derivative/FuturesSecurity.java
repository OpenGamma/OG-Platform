/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.money.Currency;

/**
 * Abstract class for generic securities with a futures-style margining feature.
 */
public abstract class FuturesSecurity implements InstrumentDerivative {

  /**
   * The last trading time.
   */
  private final double _tradingLastTime;

  /**
   * Constructor.
   * @param tradingLastTime The last trading time.
   */
  public FuturesSecurity(double tradingLastTime) {
    super();
    _tradingLastTime = tradingLastTime;
  }

  /**
   * Returns the futures last trading time.
   * @return The time.
   */
  public double getTradingLastTime() {
    return _tradingLastTime;
  }

  /**
   * Returns the currency of the futures security.
   * @return The currency.
   */
  public abstract Currency getCurrency();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_tradingLastTime);
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
    FuturesSecurity other = (FuturesSecurity) obj;
    if (Double.doubleToLongBits(_tradingLastTime) != Double.doubleToLongBits(other._tradingLastTime)) {
      return false;
    }
    return true;
  }

}
