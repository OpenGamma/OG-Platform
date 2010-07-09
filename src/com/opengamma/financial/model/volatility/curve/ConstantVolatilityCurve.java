/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConstantVolatilityCurve extends VolatilityCurve {
  private static final Logger s_logger = LoggerFactory.getLogger(ConstantVolatilityCurve.class);
  private final double _sigma;

  public ConstantVolatilityCurve(final double sigma) {
    ArgumentChecker.notNegative(sigma, "sigma");
    _sigma = sigma;
  }

  @Override
  public Double getVolatility(final Double t) {
    Validate.notNull(t, "t");
    ArgumentChecker.notNegative(t, "t");
    return _sigma;
  }

  @Override
  public Set<Double> getXData() {
    return Collections.emptySet();
  }

  @Override
  public VolatilityCurve withMultipleShifts(final Map<Double, Double> shifts) {
    Validate.notNull(shifts);
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning unchanged curve");
      return new ConstantVolatilityCurve(_sigma);
    }
    if (shifts.size() != 1) {
      s_logger.warn("Shift map contained more than one element - only using first in time");
    }
    Map<Double, Double> sorted = new TreeMap<Double, Double>(shifts);
    final Map.Entry<Double, Double> firstEntry = sorted.entrySet().iterator().next();
    Validate.notNull(firstEntry);
    ArgumentChecker.notNegative(firstEntry.getKey(), "time");
    Validate.notNull(firstEntry.getValue());
    return new ConstantVolatilityCurve(_sigma + firstEntry.getValue());
  }

  @Override
  public VolatilityCurve withParallelShift(final Double shift) {
    Validate.notNull(shift);
    return new ConstantVolatilityCurve(_sigma + shift);
  }

  @Override
  public VolatilityCurve withSingleShift(final Double x, final Double shift) {
    Validate.notNull(x);
    Validate.notNull(shift);
    ArgumentChecker.notNegative(x, "time");
    return new ConstantVolatilityCurve(_sigma + shift);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_sigma);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ConstantVolatilityCurve other = (ConstantVolatilityCurve) obj;
    if (Double.doubleToLongBits(_sigma) != Double.doubleToLongBits(other._sigma)) {
      return false;
    }
    return true;
  }

}
