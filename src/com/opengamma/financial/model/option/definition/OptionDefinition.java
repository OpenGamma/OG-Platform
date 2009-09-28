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
public abstract class OptionDefinition<T extends StandardOptionDataBundle> {
  private final Double _strike;
  private final Expiry _expiry;
  private final Boolean _isCall;
  protected Function1D<T, Boolean> _exerciseFunction;
  protected Function1D<T, Double> _payoffFunction;

  /**
   * 
   * @param strike
   * @param expiry
   * @param isCall
   */
  public OptionDefinition(Double strike, Expiry expiry, Boolean isCall) {
    _strike = strike;
    _expiry = expiry;
    _isCall = isCall;
    initPayoffAndExerciseFunctions();
  }

  protected abstract void initPayoffAndExerciseFunctions();

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
  public double getTimeToExpiry(ZonedDateTime date) {
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
   * @throws IllegalArgumentException
   *           If the exercise function has not been initialised in the
   *           descendant class.
   */
  public Function1D<T, Boolean> getExerciseFunction() {
    if (_exerciseFunction == null)
      throw new IllegalArgumentException("Exercise function was not initialised");
    return _exerciseFunction;
  }

  /**
   * 
   * @return The payoff function.
   * @throws IllegalArgumentException
   *           If the payoff function has not been initialised in the descendant
   *           class.
   */
  public Function1D<T, Double> getPayoffFunction() {
    if (_payoffFunction == null)
      throw new IllegalArgumentException("Payoff function was not initialised");
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_exerciseFunction == null ? 0 : _exerciseFunction.hashCode());
    result = prime * result + (_expiry == null ? 0 : _expiry.hashCode());
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_payoffFunction == null ? 0 : _payoffFunction.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final OptionDefinition other = (OptionDefinition) obj;
    if (_exerciseFunction == null) {
      if (other._exerciseFunction != null)
        return false;
    } else if (!_exerciseFunction.equals(other._exerciseFunction))
      return false;
    if (_expiry == null) {
      if (other._expiry != null)
        return false;
    } else if (!_expiry.equals(other._expiry))
      return false;
    if (_isCall != other._isCall)
      return false;
    if (_payoffFunction == null) {
      if (other._payoffFunction != null)
        return false;
    } else if (!_payoffFunction.equals(other._payoffFunction))
      return false;
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike))
      return false;
    return true;
  }
}
