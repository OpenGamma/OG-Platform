package com.opengamma.financial.model.option.definition;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
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

  @Override
  public Function1D<OptionDataBundleWithOptionPrice, Boolean> getExerciseFunction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPayoffFunction() {
    // TODO Auto-generated method stub
    return null;
  }
}
