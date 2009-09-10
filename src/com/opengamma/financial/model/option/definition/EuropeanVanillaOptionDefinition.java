package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;

public class EuropeanVanillaOptionDefinition extends OptionDefinition {

  public EuropeanVanillaOptionDefinition(double strike, Date expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<Double, Double, Exception>() {

      @Override
      public Double evaluate(Double spot) {
        return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
      }

    };
    _exerciseFunction = new Function<Double, Boolean, Exception>() {

      @Override
      public Boolean evaluate(Double... x) {
        return false;
      }
    };
  }
}
