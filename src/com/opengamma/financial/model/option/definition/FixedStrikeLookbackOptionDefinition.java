/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeriesOperations;
import com.opengamma.util.time.Expiry;

/**
 * A fixed-strike lookback call(put) option pays out the maximum of the
 * difference between the highest(lowest) observed price of the
 * underlying(strike) and the strike(minimum observed price of the underlying)
 * and zero.
 * 
 * @author emcleod
 */
public class FixedStrikeLookbackOptionDefinition extends OptionDefinition {
  private final Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double> _payoffFunction = new Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundleWithSpotTimeSeries data) {
      return isCall() ? Math.max(0, DoubleTimeSeriesOperations.maxValue(data.getSpotTimeSeries()) - getStrike()) : Math.max(0, getStrike()
          - DoubleTimeSeriesOperations.minValue(data.getSpotTimeSeries()));
    }

  };
  private final Function1D<OptionDataBundleWithOptionPrice, Boolean> _exerciseFunction = new Function1D<OptionDataBundleWithOptionPrice, Boolean>() {

    @Override
    public Boolean evaluate(final OptionDataBundleWithOptionPrice x) {
      return false;
    }

  };

  public FixedStrikeLookbackOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  public Function1D<OptionDataBundleWithOptionPrice, Boolean> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double> getPayoffFunction() {
    return _payoffFunction;
  }
}
