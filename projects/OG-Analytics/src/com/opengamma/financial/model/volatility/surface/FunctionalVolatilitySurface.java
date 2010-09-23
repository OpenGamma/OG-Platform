/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 */
public class FunctionalVolatilitySurface extends VolatilitySurface {
  private final Function1D<DoublesPair, Double> _volatilityFunction;

  public FunctionalVolatilitySurface(final Function1D<DoublesPair, Double> volatilityFunction) {
    Validate.notNull(volatilityFunction);
    _volatilityFunction = volatilityFunction;
  }

  @Override
  public Double getVolatility(final DoublesPair xy) {
    Validate.notNull(xy, "xy");
    return _volatilityFunction.evaluate(xy);
  }

  @Override
  public VolatilitySurface withMultipleShifts(final Map<DoublesPair, Double> shifts) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilitySurface withParallelShift(final double shift) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilitySurface withSingleShift(final DoublesPair xy, final double shift) {
    throw new NotImplementedException();
  }

  public Function1D<DoublesPair, Double> getVolatilityFunction() {
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
    final FunctionalVolatilitySurface other = (FunctionalVolatilitySurface) obj;
    return ObjectUtils.equals(_volatilityFunction, other._volatilityFunction);
  }
}
