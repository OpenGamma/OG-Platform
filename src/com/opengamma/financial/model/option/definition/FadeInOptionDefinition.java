/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Iterator;

import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * Definition for a fade-in option. The payoff of the option is the same as that
 * for a standard option with the size of the payoff weighted by how many
 * fixings the asset price were inside a pre-defined range <i>(L, U)</i>
 * 
 * @author emcleod
 */
public class FadeInOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      final DoubleTimeSeries<?> spotTS = data.getSpotTimeSeries();
      final Iterator<Double> iter = spotTS.valuesIterator();
      double inRange = 0;
      while (iter.hasNext()) {
        inRange = iter.next();
        if (inRange > getLowerBound() && inRange < getUpperBound()) {
          inRange++;
        }
      }
      return inRange / spotTS.size() * (isCall() ? Math.max(0, data.getSpot() - getStrike()) : Math.max(0, getStrike() - data.getSpot()));
    }
  };
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public Boolean shouldExercise(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      return false;
    }
  };
  // TODO maybe use a barrier here?
  private final double _lowerBound;
  private final double _upperBound;

  public FadeInOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double lowerBound, final double upperBound) {
    super(strike, expiry, isCall);
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }

  public double getLowerBound() {
    return _lowerBound;
  }

  public double getUpperBound() {
    return _upperBound;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lowerBound);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_upperBound);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final FadeInOptionDefinition other = (FadeInOptionDefinition) obj;
    if (Double.doubleToLongBits(_lowerBound) != Double.doubleToLongBits(other._lowerBound))
      return false;
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound))
      return false;
    return true;
  }

  @Override
  public OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }
}
