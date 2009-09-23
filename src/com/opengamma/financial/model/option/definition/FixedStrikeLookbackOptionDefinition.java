package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 */

public class FixedStrikeLookbackOptionDefinition extends OptionDefinition {

  public FixedStrikeLookbackOptionDefinition(double strike, Expiry expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }

  /*
   * @Override public double getPayoff(double... inputs) {
   * Arrays.sort(inputs);// TODO double-check which way the sort goes return
   * isCall() ? Math.max(inputs[inputs.length - 1] - getStrike(), 0) :
   * Math.max(getStrike() - inputs[0], 0); }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */
}
