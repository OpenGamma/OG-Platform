/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * A fixed-strike lookback call(put) option pays out the maximum of the
 * difference between the highest(lowest) observed price of the
 * underlying(strike) and the strike(minimum observed price of the underlying)
 * and zero.
 * 
 * @author emcleod
 */
public class FixedStrikeLookbackOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundleWithSpotTimeSeries> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundleWithSpotTimeSeries>() {

    @Override
    public Double getPayoff(final StandardOptionDataBundleWithSpotTimeSeries data, final Double optionPrice) {
      final FastLongDoubleTimeSeries ts = data.getSpotTimeSeries().toFastLongDoubleTimeSeries();
      return isCall() ? Math.max(0, ts.maxValue() - getStrike()) : Math.max(0, getStrike() - ts.minValue());
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundleWithSpotTimeSeries> _exerciseFunction = new OptionExerciseFunction<StandardOptionDataBundleWithSpotTimeSeries>() {

    @Override
    public Boolean shouldExercise(final StandardOptionDataBundleWithSpotTimeSeries data, final Double optionPrice) {
      return false;
    }
  };

  public FixedStrikeLookbackOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundleWithSpotTimeSeries> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundleWithSpotTimeSeries> getPayoffFunction() {
    return _payoffFunction;
  }
}
