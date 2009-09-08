package com.opengamma.financial.model.option.definition;

import java.util.Date;

public class LogOptionDefinition extends OptionDefinition {

  public LogOptionDefinition(double strike, Date expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }

  /*
   * @Override public double getPayoff(double... spot) { // TODO put/call return
   * Math.max(Math.log(spot[0] / getStrike()), 0); }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */
}
