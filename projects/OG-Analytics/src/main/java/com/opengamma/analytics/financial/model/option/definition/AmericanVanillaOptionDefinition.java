/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
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
 */
public class AmericanVanillaOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(optionPrice);
      ArgumentChecker.notNegative(optionPrice, "option price");
      final double spot = data.getSpot();
      return isCall() ? Math.max(optionPrice, spot - getStrike()) : Math.max(optionPrice, getStrike() - spot);
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new OptionExerciseFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public boolean shouldExercise(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(optionPrice);
      ArgumentChecker.notNegative(optionPrice, "option price");
      return optionPrice < _payoffFunction.getPayoff(data, optionPrice);
    }
  };

  /**
   * 
   * @param strike The option strike
   * @param expiry The option expiry
   * @param isCall Whether the option is a put or call
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

  @Override
  public String toString() {
    return "Vanilla American" + (isCall() ? " call " : " put ") + "[K = " + getStrike() + ", " + getExpiry() + "]";
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return super.equals(obj);
  }

}
