/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * Contains the minimum information required to define an option. Descendant
 * classes must define a payoff function, which gives the payoff given a
 * particular spot value, and an exercise function, which calculates whether an
 * option should be exercised.
 * 
 * @author emcleod
 */
public abstract class OptionDefinition {
  private final Double _strike;
  private final Expiry _expiry;
  private final Boolean _isCall;

  /**
   * 
   * @param strike
   * @param expiry
   * @param isCall
   */
  public OptionDefinition(final Double strike, final Expiry expiry, final Boolean isCall) {
    _strike = strike;
    _expiry = expiry;
    _isCall = isCall;
  }

  /**
   * 
   * @return Returns the strike.
   */
  public Double getStrike() {
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
    return DateUtil.getDifferenceInYears(date, getExpiry().getExpiry());
  }

  /**
   * 
   * @return Returns true if the option is a call.
   */
  public Boolean isCall() {
    return _isCall;
  }

  /**
   * 
   * @return The exercise function.
   */
  public abstract <T> Function1D<? super OptionDataBundleWithOptionPrice, Boolean> getExerciseFunction();

  /**
   * 
   * @return The payoff function.
   */
  public abstract <T> Function1D<? super StandardOptionDataBundle, Double> getPayoffFunction();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_expiry == null ? 0 : _expiry.hashCode());
    result = prime * result + (_isCall == null ? 0 : _isCall.hashCode());
    result = prime * result + (_strike == null ? 0 : _strike.hashCode());
    return result;
  }

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
    if (_isCall == null) {
      if (other._isCall != null)
        return false;
    } else if (!_isCall.equals(other._isCall))
      return false;
    if (_strike == null) {
      if (other._strike != null)
        return false;
    } else if (!_strike.equals(other._strike))
      return false;
    return true;
  }
}
