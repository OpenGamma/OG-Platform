/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class FunctionalVolatilityCurve extends VolatilityCurve {
  private final Function1D<Double, Double> _volatilityFunction;

  public FunctionalVolatilityCurve(final Function1D<Double, Double> volatilityFunction) {
    Validate.notNull(volatilityFunction);
    _volatilityFunction = volatilityFunction;
  }

  @Override
  public Double getVolatility(final Double x) {
    Validate.notNull(x, "x");
    return _volatilityFunction.evaluate(x);
  }

  @Override
  public VolatilityCurve withMultipleShifts(final Map<Double, Double> shifts) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilityCurve withParallelShift(final Double shift) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilityCurve withSingleShift(final Double x, final Double shift) {
    throw new NotImplementedException();
  }

  public Function1D<Double, Double> getVolatilityFunction() {
    return _volatilityFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_volatilityFunction == null) ? 0 : _volatilityFunction.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FunctionalVolatilityCurve other = (FunctionalVolatilityCurve) obj;
    return ObjectUtils.equals(_volatilityFunction, other._volatilityFunction);
  }
}
