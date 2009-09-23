package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 */

public class EuropeanSingleBarrierOptionDefinition extends OptionDefinition {
  private Barrier _barrier;
  private EuropeanVanillaOptionDefinition _underlyingOption;

  public EuropeanSingleBarrierOptionDefinition(double strike, Expiry expiry, boolean isCall, Barrier barrier) {
    super(strike, expiry, isCall);
    _barrier = barrier;
    _underlyingOption = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
  }

  public Barrier getBarrier() {
    return _barrier;
  }

  /*
   * @Override public double getPayoff(double... varArgs) { // TODO
   * Auto-generated method stub return 0; }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }
}
