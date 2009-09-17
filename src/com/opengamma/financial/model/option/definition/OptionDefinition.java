package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.math.function.Function;
import com.opengamma.util.time.DateUtil;

public abstract class OptionDefinition {
  protected final double DAYS_IN_YEAR = 365.25;
  private final double _strike;
  private final Date _expiry;
  private final boolean _isCall;
  protected Function<Double, Double, ? extends Exception> _payoffFunction;
  protected Function<Double, Boolean, ? extends Exception> _exerciseFunction;

  public OptionDefinition(Double strike, Date expiry, Boolean isCall) {
    _strike = strike;
    _expiry = expiry;
    _isCall = isCall;
    initPayoffAndExerciseFunctions();
  }

  protected abstract void initPayoffAndExerciseFunctions();

  public Double getStrike() {
    return _strike;
  }

  public Date getExpiry() {
    return _expiry;
  }

  public double getTimeToExpiry(Date date) {
    return DateUtil.subtract(getExpiry(), date) / DAYS_IN_YEAR;
  }

  public Boolean isCall() {
    return _isCall;
  }

  public Function<Double, Boolean, ? extends Exception> getExerciseFunction() {
    if (_exerciseFunction == null)
      throw new IllegalArgumentException("Exercise function was not initialised");
    return _exerciseFunction;
  }

  public Function<Double, Double, ? extends Exception> getPayoffFunction() {
    if (_payoffFunction == null)
      throw new IllegalArgumentException("Payoff function was not initialised");
    return _payoffFunction;
  }
}
