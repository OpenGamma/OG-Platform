package com.opengamma.financial.model.option.definition;

import java.util.Date;

public class PoweredOptionDefinition extends OptionDefinition {
  private double _power;

  public PoweredOptionDefinition(double strike, Date expiry, double power, boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
  }

  /*
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   * 
   * @Override public double getPayoff(double... spot) { return
   * Math.pow(isCall() ? Math.max(spot[0] - getStrike(), 0) :
   * Math.max(getStrike() - spot[0], 0), _power); }
   */

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }
}
