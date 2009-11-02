/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.math.function.Function1D;
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
 * @author emcleod
 */

public class CappedPowerOptionDefinition extends OptionDefinition {
  private final Function1D<StandardOptionDataBundle, Double> _payoffFunction = new Function1D<StandardOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundle data) {
      final double spot = data.getSpot();
      return isCall() ? Math.min(Math.max(Math.pow(spot, getPower()) - getStrike(), 0), getCap()) : Math.min(Math.max(getStrike() - Math.pow(spot, getPower()), 0), getCap());
    }

  };
  private final Function1D<OptionDataBundleWithOptionPrice, Boolean> _exerciseFunction = new Function1D<OptionDataBundleWithOptionPrice, Boolean>() {

    @Override
    public Boolean evaluate(final OptionDataBundleWithOptionPrice data) {
      return false;
    }

  };
  private final double _power;
  private final double _cap;

  /**
   * 
   * @param strike
   * @param expiry
   * @param power
   * @param cap
   * @param isCall
   */
  public CappedPowerOptionDefinition(final double strike, final Expiry expiry, final double power, final double cap, final boolean isCall) {
    super(strike, expiry, isCall);
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

  @Override
  public Function1D<OptionDataBundleWithOptionPrice, Boolean> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_cap);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_power);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final CappedPowerOptionDefinition other = (CappedPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_cap) != Double.doubleToLongBits(other._cap))
      return false;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power))
      return false;
    return true;
  }
}
