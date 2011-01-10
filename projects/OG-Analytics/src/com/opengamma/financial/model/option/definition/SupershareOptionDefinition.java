/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class SupershareOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s = data.getSpot();
      return ArgumentChecker.isInRangeExcludingHigh(_lowerBound, _upperBound, s) ? s / _lowerBound : 0;
    }
  };
  private final double _lowerBound;
  private final double _upperBound;

  public SupershareOptionDefinition(final Expiry expiry, final double lowerBound, final double upperBound) {
    super(null, expiry, null);
    ArgumentChecker.notNegative(lowerBound, "lower bound");
    ArgumentChecker.notNegative(upperBound, "upper bound");
    if (lowerBound >= upperBound) {
      throw new IllegalArgumentException("Lower bound must be less than upper bound");
    }
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
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
    final SupershareOptionDefinition other = (SupershareOptionDefinition) obj;
    if (Double.doubleToLongBits(_lowerBound) != Double.doubleToLongBits(other._lowerBound)) {
      return false;
    }
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound)) {
      return false;
    }
    return true;
  }

}
