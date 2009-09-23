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
public class AsymmetricPowerOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {
  private final double _power;

  /**
   * 
   * @param strike
   * @param expiry
   * @param power
   * @param isCall
   */
  public AsymmetricPowerOptionDefinition(double strike, Expiry expiry, double power, boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Double>() {

      @Override
      public Double evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> data) {
        double spot = data.getDataBundle().getSpot();
        return isCall() ? Math.max(0, Math.pow(spot, getPower()) - getStrike()) : Math.max(0, getStrike() - Math.pow(spot, getPower()));
      }

    };

    _exerciseFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Boolean>() {

      @Override
      public Boolean evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> x) {
        return false;
      }

    };
  }

  /**
   * 
   * @return The value of the power.
   */
  public double getPower() {
    return _power;
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
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AsymmetricPowerOptionDefinition other = (AsymmetricPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power))
      return false;
    return true;
  }
}
