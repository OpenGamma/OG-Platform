package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
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

  /*
   * @Override public double getPayoff(double... inputs) { double weight = 0;
   * int n = inputs.length; for (int i = 0; i < n - 1; i++) { if (inputs[i] >
   * _lowerBound && inputs[i] < _upperBound) { weight++; } } return weight /
   * (inputs.length - 1) * (isCall() ? Math.max(inputs[n - 1] - getStrike(), 0)
   * : Math.max(getStrike() - inputs[n - 1], 0)); }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */
}
