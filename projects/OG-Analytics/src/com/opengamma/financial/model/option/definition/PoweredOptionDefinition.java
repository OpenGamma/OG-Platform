/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for a powered option. The exercise style is European.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> and power
 * <i>i</i> has payoff <i>max(0, S - K)<sup>i</sup></i> for a call and <i>max(0,
 * K - S)<sup>i</sup></i> for a put.
 * 
 */
public class PoweredOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double spot = data.getSpot();
      return isCall() ? Math.pow(Math.max(0, spot - getStrike()), getPower()) : Math.pow(Math.max(0, getStrike() - spot), getPower());
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final double _power;

  /**
   * 
   * @param strike The option strike
   * @param expiry The option expiry
   * @param power The power to which the payoff is raised
   * @param isCall Is call or put
   */
  public PoweredOptionDefinition(final double strike, final Expiry expiry, final double power, final boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
  }

  /**
   * 
   * @return The value of the power.
   */
  public double getPower() {
    return _power;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_power);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PoweredOptionDefinition other = (PoweredOptionDefinition) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }
}
