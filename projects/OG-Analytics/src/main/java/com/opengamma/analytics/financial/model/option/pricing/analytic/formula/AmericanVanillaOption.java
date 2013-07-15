/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AmericanVanillaOption {
  private final boolean _isCall;
  private final double _timeToExpiry;
  private final double _strike;

  public AmericanVanillaOption(final double strike, final double timeToExpiry, final boolean isCall) {
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "time to expiry must be >= 0.0");
    _strike = strike;
    _timeToExpiry = timeToExpiry;
    _isCall = isCall;
  }

  public boolean isCall() {
    return _isCall;
  }

  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  public double getStrike() {
    return _strike;
  }

  /**
   * Computes the pay-off for a spot price at expiry.
   * @param spot The spot price.
   * @return The pay-off.
   */
  public double getPayoff(final double spot) {
    return isCall() ? Math.max(0, spot - _strike) : Math.max(0, _strike - spot);
  }

  public static AmericanVanillaOption fromDefinition(final AmericanVanillaOptionDefinition definition, final ZonedDateTime date) {
    Validate.notNull(definition, "definition");
    Validate.notNull(date, "date");
    return new AmericanVanillaOption(definition.getStrike(), definition.getTimeToExpiry(date), definition.isCall());
  }

  public AmericanVanillaOption withStrike(final double strike) {
    return new AmericanVanillaOption(strike, _timeToExpiry, _isCall);
  }

  public AmericanVanillaOption withTimeToExpiry(final double timeToExpiry) {
    return new AmericanVanillaOption(_strike, timeToExpiry, _isCall);
  }

  public AmericanVanillaOption withIsCall(final boolean isCall) {
    return new AmericanVanillaOption(_strike, _timeToExpiry, isCall);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToExpiry);
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
    final AmericanVanillaOption other = (AmericanVanillaOption) obj;
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    return true;
  }

}
