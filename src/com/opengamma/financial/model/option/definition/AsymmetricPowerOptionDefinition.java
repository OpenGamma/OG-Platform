/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for an asymmetric power options (a.k.a standard power options).
 * The exercise style is European.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> and power
 * <i>i</i> (where <i>i > 0</i>) has payoff <i>max(S<sup>i</sup> - K, 0)</i> for
 * a call and <i>max(K - S<sup>i</sup>, 0)<i> for a put.
 * 
 * @author emcleod
 */
public class AsymmetricPowerOptionDefinition extends OptionDefinition {
  private final Function1D<StandardOptionDataBundle, Double> _payoffFunction = new Function1D<StandardOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundle data) {
      final double spot = data.getSpot();
      return isCall() ? Math.max(0, Math.pow(spot, getPower()) - getStrike()) : Math.max(0, getStrike() - Math.pow(spot, getPower()));
    }

  };
  private final Function1D<OptionDataBundleWithOptionPrice, Boolean> _exerciseFunction = new Function1D<OptionDataBundleWithOptionPrice, Boolean>() {

    @Override
    public Boolean evaluate(final OptionDataBundleWithOptionPrice x) {
      return false;
    }

  };
  private final double _power;

  /**
   * 
   * @param strike
   * @param expiry
   * @param power
   * @param isCall
   */
  public AsymmetricPowerOptionDefinition(final double strike, final Expiry expiry, final double power, final boolean isCall) {
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
    final AsymmetricPowerOptionDefinition other = (AsymmetricPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power))
      return false;
    return true;
  }
}
