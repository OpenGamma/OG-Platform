/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.util.Iterator;

import org.apache.commons.lang.Validate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for a fade-in option. The payoff of the option is the same as that
 * for a standard option with the size of the payoff weighted by how many
 * times the asset price was inside a pre-defined range <i>(L, U)</i>
 * 
 */
public class FadeInOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      Validate.notNull(data.getSpotTimeSeries());
      final DoubleTimeSeries<?> spotTS = data.getSpotTimeSeries();
      final Iterator<Double> iter = spotTS.valuesIterator();
      int inRange = 0;
      double value = 0;
      while (iter.hasNext()) {
        value = iter.next();
        if (value > getLowerBound() && value < getUpperBound()) {
          inRange++;
        }
      }
      return inRange * (isCall() ? Math.max(0, data.getSpot() - getStrike()) : Math.max(0, getStrike() - data.getSpot())) / spotTS.size();
    }
  };
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final double _lowerBound;
  private final double _upperBound;

  public FadeInOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double lowerBound, final double upperBound) {
    super(strike, expiry, isCall);
    ArgumentChecker.notNegative(lowerBound, "lower bound");
    ArgumentChecker.notNegative(upperBound, "upper bound");
    if (upperBound < lowerBound) {
      throw new IllegalArgumentException("Upper bound was less than lower bound");
    }
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
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_upperBound);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FadeInOptionDefinition other = (FadeInOptionDefinition) obj;
    if (Double.doubleToLongBits(_lowerBound) != Double.doubleToLongBits(other._lowerBound)) {
      return false;
    }
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound)) {
      return false;
    }
    return true;
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
