/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class VolatilitySurface implements VolatilityModel<DoublesPair> {

  @Override
  public abstract Double getVolatility(DoublesPair xy);

  public abstract VolatilitySurface withParallelShift(double shift);

  public abstract VolatilitySurface withSingleShift(DoublesPair xy, double shift);

  public abstract VolatilitySurface withMultipleShifts(Map<DoublesPair, Double> shifts);
}
