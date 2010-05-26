/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * Contains the minimum information required to define an option. Descendant
 * classes must define a payoff function, which gives the payoff given a
 * particular spot value, and an exercise function, which calculates whether an
 * option should be exercised.
 * 
 */
public abstract class OptionDefinition {
  private final double _strike;
  private final Expiry _expiry;
  private final boolean _isCall;

  /**
   * 
   * @param strike
   * @param expiry
   * @param isCall
   */
  public OptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    ArgumentChecker.notNegative(strike, "strike");
    Validate.notNull(expiry);
    _strike = strike;
    _expiry = expiry;
    _isCall = isCall;
  }

  /**
   * 
   * @return Returns the strike.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * 
   * @return Returns the expiry.
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * 
   * @param date
   * @return The time to expiry in years, where a year is defined as 365.25
   */
  public double getTimeToExpiry(final ZonedDateTime date) {
    if (date.isAfter(getExpiry().getExpiry())) {
      throw new IllegalArgumentException("Date " + date + " is after expiry " + getExpiry());
    }
    return DateUtil.getDifferenceInYears(date, getExpiry().getExpiry());
  }

  /**
   * 
   * @return Returns true if the option is a call.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * 
   * @return The exercise function.
   */
  public abstract <T extends StandardOptionDataBundle> OptionExerciseFunction<T> getExerciseFunction();

  /**
   * 
   * @return The payoff function.
   */
  public abstract <T extends StandardOptionDataBundle> OptionPayoffFunction<T> getPayoffFunction();

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expiry == null) ? 0 : _expiry.hashCode());
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final OptionDefinition other = (OptionDefinition) obj;
    if (_expiry == null) {
      if (other._expiry != null)
        return false;
    } else if (!_expiry.equals(other._expiry))
      return false;
    if (_isCall != other._isCall)
      return false;
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike))
      return false;
    return true;
  }
}
