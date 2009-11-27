/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for an American-style vanilla option. An American-style option can
 * be exercised at any time up to expiry.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> has payoff
 * <i>max(0, S - K)</i> for a call and <i>max(0, K - S)</i> for a put. If the
 * price of the option is <i>O</i>, then the option should be exercised early if
 * <i>O > K - S</i> for a call and <i>O > S - K</i> for a put.
 * 
 * @author emcleod
 */
public class AmericanVanillaOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public Double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      final double spot = data.getSpot();
      return isCall() ? Math.max(optionPrice, spot - getStrike()) : Math.max(optionPrice, getStrike() - spot);
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new OptionExerciseFunction<StandardOptionDataBundle>() {

    @Override
    public Boolean shouldExercise(final StandardOptionDataBundle data, final Double optionPrice) {
      final double spot = data.getSpot();
      return isCall() ? optionPrice < spot - getStrike() : optionPrice < getStrike() - spot;
    }
  };

  /**
   * 
   * @param strike
   * @param expiry
   * @param isCall
   */
  public AmericanVanillaOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }
}
