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
 * 
 * A floating-strike lookback call(put) option gives the holder the right to
 * buy(sell) the underlying security at the lowest(highest) price observed
 * during the option's lifetime.
 * <p>
 * If the spot price is <i>S</i>, the payoff from a call is <i>S -
 * S<sub>min</sub></i>, where <i>S<sub>min</sub> is the minimum observed price.
 * The payoff from a put is <i>S<sub>max</sub> - S</i>, where <i>S<sub>max</sub>
 * is the maximum observed price.
 * 
 * @author emcleod
 */
public class FloatingStrikeLookbackOptionDefinition extends OptionDefinition {
  private final Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double> _payoffFunction = new Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundleWithSpotTimeSeries data) {
      return isCall() ? data.getSpot() - DoubleTimeSeriesOperations.minValue(data.getSpotTimeSeries()) : DoubleTimeSeriesOperations.maxValue(data.getSpotTimeSeries())
          - data.getSpot();
    }

  };
  private final Function1D<OptionDataBundleWithOptionPrice, Boolean> _exerciseFunction = new Function1D<OptionDataBundleWithOptionPrice, Boolean>() {

    @Override
    public Boolean evaluate(final OptionDataBundleWithOptionPrice x) {
      return false;
    }

  };

  public FloatingStrikeLookbackOptionDefinition(final Expiry expiry, final boolean isCall) {
    super(null, expiry, isCall);
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
