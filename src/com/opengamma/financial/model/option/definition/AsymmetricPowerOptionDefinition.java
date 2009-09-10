package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;

public class AsymmetricPowerOptionDefinition extends OptionDefinition {
  private final double _power;

  public AsymmetricPowerOptionDefinition(double strike, Date expiry, double power, boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<Double, Double, Exception>() {

      @Override
      public Double evaluate(Double spot) {
        return isCall() ? Math.max(Math.pow(spot, getPower()) - getStrike(), 0) : Math.max(getStrike() - Math.pow(spot, getPower()), 0);
      }

    };

    _exerciseFunction = new Function<Double, Boolean, Exception>() {

      @Override
      public Boolean evaluate(Double... x) {
        return false;
      }

    };
  }

  public double getPower() {
    return _power;
  }
}
