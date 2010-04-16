/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;
import java.util.Set;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @author emcleod
 */
public abstract class VolatilitySurface implements VolatilityModel<Pair<Double, Double>> {

  @Override
  public abstract Double getVolatility(Pair<Double, Double> xy);

  public abstract Set<Pair<Double, Double>> getXYData();

  public abstract VolatilitySurface withParallelShift(Double shift);

  public abstract VolatilitySurface withSingleShift(Pair<Double, Double> xy, Double shift);

  public abstract VolatilitySurface withMultipleShifts(Map<Pair<Double, Double>, Double> shifts);
}
