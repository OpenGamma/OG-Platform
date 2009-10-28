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
public class EuropeanVanillaOptionDefinition extends OptionDefinition {
  private final Function1D<StandardOptionDataBundle, Double> _payoffFunction = new Function1D<StandardOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundle data) {
      final double spot = data.getSpot();
      return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
    }

  };
  private final Function1D<OptionDataBundleWithOptionPrice, Boolean> _exerciseFunction = new Function1D<OptionDataBundleWithOptionPrice, Boolean>() {

    @Override
    public Boolean evaluate(final OptionDataBundleWithOptionPrice data) {
      return false;
    }
  };

  public EuropeanVanillaOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  public Function1D<OptionDataBundleWithOptionPrice, Boolean> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPayoffFunction() {
    return _payoffFunction;
  }
}
