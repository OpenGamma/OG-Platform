/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class StudentTVaRParameters {
  private final double _horizon;
  private final double _periods;
  private final double _quantile;
  private final double _dof;
  private final double _mult;
  private final double _scale;
  private final ProbabilityDistribution<Double> _studentT;
  
  public StudentTVaRParameters(final double horizon, final double periods, final double quantile, final double dof) {
    Validate.isTrue(horizon > 0, "horizon");
    Validate.isTrue(periods > 0, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    Validate.isTrue(dof > 0, "degrees of freedom");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
    _mult = Math.sqrt((_dof - 2) * horizon / dof / periods) * _studentT.getInverseCDF(quantile);
    _scale = horizon / periods;
  }
  
  public double getMult() {
    return _mult;
  }
  
  public double getScale() {
    return _scale;
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
  
  public double getDegreesOfFreedom() {
    return _dof;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_dof);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
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
    StudentTVaRParameters other = (StudentTVaRParameters) obj;
    if (Double.doubleToLongBits(_dof) != Double.doubleToLongBits(other._dof)) {
      return false;
    }
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
