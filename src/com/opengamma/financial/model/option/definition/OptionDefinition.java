/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.InstantProvider;

import com.opengamma.math.function.Function;
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
  private final double _strike;
  private final Expiry _expiry;
  private final boolean _isCall;
  protected Function<Double, Double> _payoffFunction;
  protected Function<Double, Boolean> _exerciseFunction;

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
  public double getTimeToExpiry(InstantProvider date) {
    return DateUtil.getDifferenceInYears(getExpiry(), date);
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
  public Function<Double, Boolean> getExerciseFunction() {
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
  public Function<Double, Double> getPayoffFunction() {
    if (_payoffFunction == null)
      throw new IllegalArgumentException("Payoff function was not initialised");
    return _payoffFunction;
  }
}
