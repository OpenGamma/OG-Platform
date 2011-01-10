/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * A capped power option is a power option with a maximum payoff. The exercise
 * style is European.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i>, power
 * <i>i</i> and capped at a maximum of <i>C</i> has payoff <i>min[max(0,
 * S<sup>i</sup> - K), C]</i> for a call and <i>min[max(0, K - S<sup>i</sup>),
 * C]</i> for a put.
 * 
 */

public class CappedPowerOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double spot = data.getSpot();
      return isCall() ? Math.min(Math.max(Math.pow(spot, getPower()) - getStrike(), 0), getCap()) : Math.min(Math.max(getStrike() - Math.pow(spot, getPower()), 0), getCap());
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final double _power;
  private final double _cap;

  /**
   * 
   * @param strike The option strike
   * @param expiry The option expiry
   * @param power The power 
   * @param cap The cap
   * @param isCall Is the option a put or call
   */
  public CappedPowerOptionDefinition(final double strike, final Expiry expiry, final double power, final double cap, final boolean isCall) {
    super(strike, expiry, isCall);
    ArgumentChecker.notNegative(cap, "cap");
    if (!isCall && cap > strike) {
      throw new IllegalArgumentException("Cannot have cap larger than strike for a put");
    }
    _power = power;
    _cap = cap;
  }

  /**
   * 
   * @return The value of the power.
   */
  public double getPower() {
    return _power;
  }

  /**
   * 
   * @return The value of the cap.
   */
  public double getCap() {
    return _cap;
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
    temp = Double.doubleToLongBits(_cap);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CappedPowerOptionDefinition other = (CappedPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_cap) != Double.doubleToLongBits(other._cap)) {
      return false;
    }
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }
}
