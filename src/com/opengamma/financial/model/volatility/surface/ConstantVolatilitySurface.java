/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.Pair;

/**
 * 
 * A VolatilitySurface that has a constant volatility for all values of x and y
 * 
 * @author emcleod
 */
public class ConstantVolatilitySurface extends VolatilitySurface {
  private static final Logger s_Log = LoggerFactory.getLogger(ConstantVolatilitySurface.class);
  private final double _sigma;

  public ConstantVolatilitySurface(final Double sigma) {
    if (sigma == null)
      throw new IllegalArgumentException("Volatility was null");
    if (sigma < 0)
      throw new IllegalArgumentException("Cannot have a negative volatility");
    _sigma = sigma;
  }

  @Override
  public Double getVolatility(final Pair<Double, Double> xy) {
    if (xy == null)
      throw new IllegalArgumentException("xy pair was null");
    if (xy.getFirst() == null)
      throw new IllegalArgumentException("x-value was null");
    if (xy.getSecond() == null)
      throw new IllegalArgumentException("y-value was null");
    return _sigma;
  }

  @Override
  public Set<Pair<Double, Double>> getXYData() {
    return Collections.<Pair<Double, Double>> emptySet();
  }

  @Override
  public VolatilitySurface withParallelShift(final Double shift) {
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    return new ConstantVolatilitySurface(_sigma + shift);
  }

  @Override
  public VolatilitySurface withSingleShift(final Pair<Double, Double> xy, final Double shift) {
    if (xy == null)
      throw new IllegalArgumentException("x-y pair was null");
    if (xy.getFirst() == null)
      throw new IllegalArgumentException("x was null");
    if (xy.getSecond() == null)
      throw new IllegalArgumentException("y was null");
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    return new ConstantVolatilitySurface(_sigma + shift);
  }

  @Override
  public VolatilitySurface withMultipleShifts(final Map<Pair<Double, Double>, Double> shifts) {
    if (shifts == null)
      throw new IllegalArgumentException("Shifts map was null");
    if (shifts.isEmpty()) {
      s_Log.info("Shift map was empty; returning unchanged surface");
      return new ConstantVolatilitySurface(_sigma);
    }
    if (shifts.size() != 1) {
      s_Log.warn("Shift map contained more than one element - only using first");
    }
    final Map.Entry<Pair<Double, Double>, Double> firstEntry = shifts.entrySet().iterator().next();
    if (firstEntry.getKey().getFirst() == null)
      throw new IllegalArgumentException("x-value for shift was null");
    if (firstEntry.getKey().getSecond() == null)
      throw new IllegalArgumentException("y-value for shift was null");
    if (firstEntry.getValue() == null)
      throw new IllegalArgumentException("Shift was null");
    return new ConstantVolatilitySurface(_sigma + firstEntry.getValue());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_sigma);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ConstantVolatilitySurface other = (ConstantVolatilitySurface) obj;
    if (Double.doubleToLongBits(_sigma) != Double.doubleToLongBits(other._sigma))
      return false;
    return true;
  }
}
