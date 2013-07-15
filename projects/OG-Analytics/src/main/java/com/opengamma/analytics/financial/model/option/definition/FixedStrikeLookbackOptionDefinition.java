/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.Expiry;

/**
 * A fixed-strike lookback call(put) option pays out the maximum of the
 * difference between the highest(lowest) observed price of the
 * underlying(strike) and the strike(minimum observed price of the underlying)
 * and zero.
 */
public class FixedStrikeLookbackOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(data.getSpotTimeSeries());
      final DoubleTimeSeries<?> ts = data.getSpotTimeSeries();
      return isCall() ? Math.max(0, ts.maxValue() - getStrike()) : Math.max(0, getStrike() - ts.minValue());
    }
  };
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();

  public FixedStrikeLookbackOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return super.equals(obj);
  }
}
