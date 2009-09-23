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
 * Definition for a European-style vanilla option.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> has payoff
 * <i>max(0, S - K)</i> for a call and <i>max(0, K - S)</i> for a put.
 * 
 * @author emcleod
 */
public class EuropeanVanillaOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {

  public EuropeanVanillaOptionDefinition(double strike, Expiry expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Double>() {

      @Override
      public Double evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> data) {
        double spot = data.getDataBundle().getSpot();
        return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
      }

    };
    _exerciseFunction = new Function1D<OptionDataBundleWithPrice<StandardOptionDataBundle>, Boolean>() {

      @Override
      public Boolean evaluate(OptionDataBundleWithPrice<StandardOptionDataBundle> data) {
        return false;
      }
    };
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 13;
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
