package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;

public class CappedPowerOptionDefinition extends OptionDefinition {
  private double _power;
  private double _cap;

  public CappedPowerOptionDefinition(double strike, Date expiry, double power, double cap, boolean isCall) {
    super(strike, expiry, isCall);
    _power = power;
    _cap = cap;
  }

  public double getPower() {
    return _power;
  }

  public double getCap() {
    return _cap;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double spot) {
        return isCall() ? Math.min(Math.max(Math.pow(spot, getPower()) - getStrike(), 0), getCap()) : Math.min(Math.max(getStrike() - Math.pow(spot, getPower()), 0), getCap());
      }

    };
    _exerciseFunction = new Function<Double, Boolean>() {

      @Override
      public Boolean evaluate(Double... x) {
        double spot = x[0];
        double option = x[1];
        return isCall() ? option > getStrike() - spot : option > spot - getStrike();
      }

    };

  }
}
