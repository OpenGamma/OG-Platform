package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 */

public class FloatingStrikeLookbackOptionDefinition extends OptionDefinition {

  public FloatingStrikeLookbackOptionDefinition(Expiry expiry, boolean isCall) {
    super(null, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }

  /*
   * @Override public double getPayoff(double... tsOfSpot) { double spot =
   * tsOfSpot[0]; double[] prices = Arrays.copyOfRange(tsOfSpot, 1,
   * tsOfSpot.length); Arrays.sort(prices);// TODO double-check which way the
   * sort goes return isCall() ? spot - prices[0] : prices[prices.length - 1] -
   * spot; }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */
}
