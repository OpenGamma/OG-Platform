package com.opengamma.financial.model.option.definition;

import javax.time.InstantProvider;

import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 */

public class ChooserOptionDefinition extends OptionDefinition {
  private InstantProvider _chooseDate;
  private StandardOptionDataBundle _vars;
  private EuropeanVanillaOptionDefinition _callDefinition;
  private EuropeanVanillaOptionDefinition _putDefinition;

  public ChooserOptionDefinition(double strike, Expiry expiry, InstantProvider chooseDate, StandardOptionDataBundle vars) {
    // TODO check expiry is after other date
    super(strike, expiry, null);
    _chooseDate = chooseDate;
    _vars = vars;
    _callDefinition = new EuropeanVanillaOptionDefinition(strike, expiry, true);
    _putDefinition = new EuropeanVanillaOptionDefinition(strike, expiry, false);
  }

  /*
   * @Override public double getPayoff(double... spot) { return
   * Math.max(MODEL.getPrice(_callDefinition, _chooseDate, spot[0], _vars),
   * MODEL.getPrice(_putDefinition, _chooseDate, spot[0], _vars)); }
   * 
   * @Override public boolean shouldExercise(double... varArgs) { return false;
   * }
   */

  @Override
  protected void initPayoffAndExerciseFunctions() {
    // TODO Auto-generated method stub

  }
}
