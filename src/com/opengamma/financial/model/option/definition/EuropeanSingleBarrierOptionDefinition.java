package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 */

public class EuropeanSingleBarrierOptionDefinition extends OptionDefinition {
  private final Barrier _barrier;
  private final EuropeanVanillaOptionDefinition _underlyingOption;

  public EuropeanSingleBarrierOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final Barrier barrier) {
    super(strike, expiry, isCall);
    _barrier = barrier;
    _underlyingOption = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
  }

  public Barrier getBarrier() {
    return _barrier;
  }

  public EuropeanVanillaOptionDefinition getUnderlyingOption() {
    return _underlyingOption;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    // TODO Auto-generated method stub
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    // TODO Auto-generated method stub
    return null;
  }
}
