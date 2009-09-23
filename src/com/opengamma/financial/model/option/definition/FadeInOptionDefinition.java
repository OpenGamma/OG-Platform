/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Iterator;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.Expiry;

/**
 * 
 * Definition for a fade-in option. The payoff of the option is the same as that
 * for a standard option with the size of the payoff weighted by how many
 * fixings the asset price were inside a pre-defined range <i>(L, U)</i>
 * 
 * @author emcleod
 */
public class FadeInOptionDefinition extends OptionDefinition<StandardOptionDataBundleWithSpotTimeSeries> {
  protected final double _lowerBound;
  protected final double _upperBound;

  public FadeInOptionDefinition(double strike, Expiry expiry, boolean isCall, double lowerBound, double upperBound) {
    super(strike, expiry, isCall);
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<StandardOptionDataBundleWithSpotTimeSeries, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundleWithSpotTimeSeries data) {
        DoubleTimeSeries spotTS = data.getSpotTimeSeries();
        Iterator<Double> iter = spotTS.valuesIterator();
        double inRange = 0;
        while (iter.hasNext()) {
          inRange = iter.next();
          if (inRange > _lowerBound && inRange < _upperBound)
            inRange++;
        }
        return inRange / spotTS.size() * (isCall() ? Math.max(0, data.getSpot() - getStrike()) : Math.max(0, getStrike() - data.getSpot()));
      }
    };

    _exerciseFunction = new Function1D<StandardOptionDataBundleWithSpotTimeSeries, Boolean>() {

      @Override
      public Boolean evaluate(StandardOptionDataBundleWithSpotTimeSeries data) {
        return false;
      }

    };
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
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    FadeInOptionDefinition other = (FadeInOptionDefinition) obj;
    if (Double.doubleToLongBits(_lowerBound) != Double.doubleToLongBits(other._lowerBound))
      return false;
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound))
      return false;
    return true;
  }
}
