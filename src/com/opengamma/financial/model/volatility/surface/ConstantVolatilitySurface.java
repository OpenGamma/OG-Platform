/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.tuple.Pair;

/**
 * 
 * A VolatilitySurface that has a constant volatility for all values of x and y
 * 
 */
public class ConstantVolatilitySurface extends VolatilitySurface implements Serializable {
  private static final Logger s_logger = LoggerFactory.getLogger(ConstantVolatilitySurface.class);
  private final double _sigma;

  public ConstantVolatilitySurface(final double sigma) {
    if (sigma < 0) {
      throw new IllegalArgumentException("Cannot have a negative volatility");
    }
    _sigma = sigma;
  }

  @Override
  public Double getVolatility(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "xy");
    Validate.notNull(xy.getFirst(), "x value");
    Validate.notNull(xy.getSecond(), "y value");
    return _sigma;
  }

  @Override
  public VolatilitySurface withParallelShift(final double shift) {
    return new ConstantVolatilitySurface(_sigma + shift);
  }

  @Override
  public VolatilitySurface withSingleShift(final Pair<Double, Double> xy, final double shift) {
    Validate.notNull(xy, "xy");
    Validate.notNull(xy.getFirst(), "x value");
    Validate.notNull(xy.getSecond(), "y value");
    return new ConstantVolatilitySurface(_sigma + shift);
  }

  @Override
  public VolatilitySurface withMultipleShifts(final Map<Pair<Double, Double>, Double> shifts) {
    Validate.notNull(shifts, "shifts map");
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning unchanged surface");
      return new ConstantVolatilitySurface(_sigma);
    }
    if (shifts.size() != 1) {
      s_logger.warn("Shift map contained more than one element - only using first");
    }
    final Map.Entry<Pair<Double, Double>, Double> firstEntry = shifts.entrySet().iterator().next();
    Validate.notNull(firstEntry.getKey().getFirst(), "x value for shift");
    Validate.notNull(firstEntry.getKey().getSecond(), "y value for shift");
    Validate.notNull(firstEntry.getValue(), "shift");
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConstantVolatilitySurface other = (ConstantVolatilitySurface) obj;
    if (Double.doubleToLongBits(_sigma) != Double.doubleToLongBits(other._sigma)) {
      return false;
    }
    return true;
  }

  public double getSigma() {
    return _sigma;
  }

}
