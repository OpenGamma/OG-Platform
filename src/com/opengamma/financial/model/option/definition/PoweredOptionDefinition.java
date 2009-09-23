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
 * Definition for a powered option. The exercise style is European.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> and power
 * <i>i</i> has payoff <i>max(0, S - K)<sup>i</sup></i> for a call and <i>max(0,
 * K - S)<sup>i</sup></i> for a put.
 * 
 * @author emcleod
 */
public class PoweredOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {
  private double _power;

  /**
   * 
   * @param strike
   * @param expiry
   * @param power
   * @param isCall
   */
  public PoweredOptionDefinition(double strike, Expiry expiry, double power, boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Double>() {

      @Override
      public Double evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> data) {
        double spot = data.getDataBundle().getSpot();
        return isCall() ? Math.pow(Math.max(0, spot - getStrike()), getPower()) : Math.pow(Math.max(0, getStrike() - spot), getPower());
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
}
