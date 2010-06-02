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
 */
public class FloatingStrikeLookbackOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(data.getSpotTimeSeries());
      final FastLongDoubleTimeSeries ts = data.getSpotTimeSeries().toFastLongDoubleTimeSeries();
      return isCall() ? data.getSpot() - ts.minValue() : ts.maxValue() - data.getSpot();
    }
  };
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Boolean shouldExercise(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      return false;
    }
  };

  public FloatingStrikeLookbackOptionDefinition(final Expiry expiry, final boolean isCall) {
    super(null, expiry, isCall);
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
