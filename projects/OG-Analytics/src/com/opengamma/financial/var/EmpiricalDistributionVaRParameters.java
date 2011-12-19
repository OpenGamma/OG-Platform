/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EmpiricalDistributionVaRParameters {
  private final double _horizon;
  private final double _periods;
  private final double _quantile;
  private final double _mult;
  private final PercentileCalculator _percentileCalculator;  

  public EmpiricalDistributionVaRParameters(final double horizon, final double periods, final double quantile) {
    Validate.isTrue(horizon > 0, "horizon");
    Validate.isTrue(periods > 0, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    _percentileCalculator = new PercentileCalculator(1 - quantile);
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _mult = Math.sqrt(horizon / periods);
  }
  
  public double getMult() {
    return _mult;
  }
  
  public PercentileCalculator getPercentileCalculator() {
    return _percentileCalculator;
  }
  
  public double getHorizon() {
    return _horizon;
  }
  
  public double getPeriods() {
    return _periods;
  }
  
  public double getQuantile() {
    return _quantile;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final EmpiricalDistributionVaRParameters other = (EmpiricalDistributionVaRParameters) obj;
    if (Double.doubleToLongBits(_horizon) != Double.doubleToLongBits(other._horizon)) {
      return false;
    }
    if (Double.doubleToLongBits(_periods) != Double.doubleToLongBits(other._periods)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantile) != Double.doubleToLongBits(other._quantile)) {
      return false;
    }
    return true;
  }
  
  
}
