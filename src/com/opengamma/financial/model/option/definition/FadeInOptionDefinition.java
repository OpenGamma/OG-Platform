/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for a fade-in option. The payoff of the option is the same as that
 * for a standard option with the size of the payoff weighted by how many
 * fixings the asset price were inside a pre-defined range <i>(L, U)</i>
 * 
 * @author emcleod
 */
public class FadeInOptionDefinition extends OptionDefinition {
  private double _lowerBound;
  private double _upperBound;

  public FadeInOptionDefinition(double strike, Expiry expiry, boolean isCall, double lowerBound, double upperBound) {
    super(strike, expiry, isCall);
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }
}
