/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;

/**
 * 
 */
public class EuropeanVanillaOption {
  private final boolean _isCall;
  private final double _t;
  private final double _k;

  public EuropeanVanillaOption(final double k, final double t, final boolean isCall) {
    //    Validate.isTrue(k > 0.0, "k must be > 0.0");
    Validate.isTrue(t >= 0.0, "t must be >= 0.0");
    _k = k;
    _t = t;
    _isCall = isCall;
  }

  public boolean isCall() {
    return _isCall;
  }

  public double getTimeToExpiry() {
    return _t;
  }

  public double getStrike() {
    return _k;
  }

  /**
   * Computes the pay-off for a spot price at expiry.
   * @param spot The spot price.
   * @return The pay-off.
   */
  public double getPayoff(final double spot) {
    return isCall() ? Math.max(0, spot - _k) : Math.max(0, _k - spot);
  }

  public static EuropeanVanillaOption fromDefinition(final EuropeanVanillaOptionDefinition definition, final ZonedDateTime date) {
    Validate.notNull(definition, "definition");
    Validate.notNull(date, "date");
    return new EuropeanVanillaOption(definition.getStrike(), definition.getTimeToExpiry(date), definition.isCall());
  }

  public EuropeanVanillaOption withStrike(final double strike) {
    return new EuropeanVanillaOption(strike, _t, _isCall);
  }

  public EuropeanVanillaOption withTimeToExpiry(final double timeToExpiry) {
    return new EuropeanVanillaOption(_k, timeToExpiry, _isCall);
  }

  public EuropeanVanillaOption withIsCall(final boolean isCall) {
    return new EuropeanVanillaOption(_k, _t, isCall);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_k);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_t);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final EuropeanVanillaOption other = (EuropeanVanillaOption) obj;
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
