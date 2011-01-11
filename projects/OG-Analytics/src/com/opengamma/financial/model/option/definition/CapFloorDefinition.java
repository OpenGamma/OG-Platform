/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CapFloorDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      return 0;
    }
  };
  private final boolean _isCap;
  private final Tenor _resetTenor;

  public CapFloorDefinition(final double strike, final boolean isCap, final Expiry expiry, final Tenor resetTenor) {
    super(strike, expiry, false);
    _isCap = isCap;
    _resetTenor = resetTenor;
  }

  public boolean isCap() {
    return _isCap;
  }

  public Tenor getResetTenor() {
    return _resetTenor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

}
