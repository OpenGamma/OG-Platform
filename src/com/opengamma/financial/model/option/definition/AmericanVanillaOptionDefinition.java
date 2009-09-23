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
public class AmericanVanillaOptionDefinition extends OptionDefinition<StandardOptionDataBundleWithOptionPrice> {

  /**
   * 
   * @param strike
   * @param expiry
   * @param isCall
   */
  public AmericanVanillaOptionDefinition(double strike, Expiry expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<StandardOptionDataBundleWithOptionPrice, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundleWithOptionPrice data) {
        final double spot = data.getSpot();
        return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
      }
    };
    _exerciseFunction = new Function1D<StandardOptionDataBundleWithOptionPrice, Boolean>() {

      @Override
      public Boolean evaluate(StandardOptionDataBundleWithOptionPrice data) {
        final double spot = data.getSpot();
        final double option = data.getOptionPrice();
        return isCall() ? option > getStrike() - spot : option > spot - getStrike();
      }
    };
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 11;
  }

  @Override
  public boolean equals(Object obj) {
    if (getClass() != obj.getClass())
      return false;
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    return true;
  }
}
