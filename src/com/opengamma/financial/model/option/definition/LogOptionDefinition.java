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
 * Definition for a log option. The exercise style is European.
 * <p>
 * When the spot price is <i>S</i>, an option with strike <i>K</i> has payoff
 * <i>max(0, ln(S / K))</i> for a call and <i>max(0, ln(K / S))</i> for a put.
 * 
 * @author emcleod
 */
public class LogOptionDefinition extends OptionDefinition<StandardOptionDataBundle> {

  public LogOptionDefinition(double strike, Expiry expiry) {
    super(strike, expiry, null);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        final double spot = data.getSpot();
        return Math.max(0, Math.log(spot / getStrike()));
      }

    };

    _exerciseFunction = new Function1D<StandardOptionDataBundle, Boolean>() {

      @Override
      public Boolean evaluate(StandardOptionDataBundle data) {
        return false;
      }

    };
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 17;
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
