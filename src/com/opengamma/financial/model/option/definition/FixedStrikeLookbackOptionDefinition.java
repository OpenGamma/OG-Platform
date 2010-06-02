/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * A fixed-strike lookback call(put) option pays out the maximum of the
 * difference between the highest(lowest) observed price of the
 * underlying(strike) and the strike(minimum observed price of the underlying)
 * and zero.
 * 
 */
public class FixedStrikeLookbackOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(data.getSpotTimeSeries());
      final FastLongDoubleTimeSeries ts = data.getSpotTimeSeries().toFastLongDoubleTimeSeries();
      return isCall() ? Math.max(0, ts.maxValue() - getStrike()) : Math.max(0, getStrike() - ts.minValue());
    }
  };
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Boolean shouldExercise(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      return false;
    }
  };

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
}
