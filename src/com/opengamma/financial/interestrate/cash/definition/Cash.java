/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Cash implements InterestRateDerivative {
  private final double _tradeTime;
  private final double _paymentTime;
  private final double _yearFraction;
  private final double _rate;
  private final String _curveName;

  public Cash(final double paymentTime, final double rate, final String yieldCurveName) {
    checkInputs(paymentTime, rate, yieldCurveName);
    _paymentTime = paymentTime;
    _curveName = yieldCurveName;
    _tradeTime = 0.0;
    _yearFraction = paymentTime;
    _rate = rate;
  }

  public Cash(final double paymentTime, final double rate, final double tradeTime, final double yearFraction, final String yieldCurveName) {
    checkInputs(paymentTime, rate, yieldCurveName);
    ArgumentChecker.notNegative(tradeTime, "trade time");
    ArgumentChecker.notNegative(yearFraction, "year fraction");
    Validate.isTrue(tradeTime < paymentTime, "Trade time must be less that payment time");
    _paymentTime = paymentTime;
    _curveName = yieldCurveName;
    _tradeTime = tradeTime;
    _yearFraction = yearFraction;
    _rate = rate;

  }

  private void checkInputs(final double paymentTime, final double rate, final String yieldCurveName) {
    ArgumentChecker.notNegative(paymentTime, "payment time");
    ArgumentChecker.notNegative(rate, "rate");
    Validate.notNull(yieldCurveName);
  }

  public double getPaymentTime() {
    return _paymentTime;
  }

  public String getYieldCurveName() {
    return _curveName;
  }

  public double getTradeTime() {
    return _tradeTime;
  }

  public double getYearFraction() {
    return _yearFraction;
  }

  public double getRate() {
    return _rate;
  }

  public <T> T accept(final InterestRateDerivativeVisitor<T> visitor, final YieldCurveBundle curves) {
    return visitor.visitCash(this, curves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_tradeTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yearFraction);
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
    Cash other = (Cash) obj;
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradeTime) != Double.doubleToLongBits(other._tradeTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_yearFraction) != Double.doubleToLongBits(other._yearFraction)) {
      return false;
    }
    return true;
  }
}
