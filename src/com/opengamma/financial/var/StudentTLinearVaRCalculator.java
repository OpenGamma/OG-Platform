/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
public class StudentTLinearVaRCalculator extends VaRCalculator<NormalStatistics<?>> {
  private double _dof;
  private double _mult;
  private double _scale;
  private ProbabilityDistribution<Double> _studentT;

  public StudentTLinearVaRCalculator(final double horizon, final double periods, final double quantile, final double dof) {
    super(horizon, periods, quantile);
    ArgumentChecker.notNegativeOrZero(dof, "degrees of freedom");
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
    setMultiplier();
  }

  @Override
  public void setHorizon(final double horizon) {
    super.setHorizon(horizon);
    setMultiplier();
  }

  @Override
  public void setPeriods(final double periods) {
    super.setPeriods(periods);
    setMultiplier();
  }

  @Override
  public void setQuantile(final double quantile) {
    super.setQuantile(quantile);
    setMultiplier();
  }

  public void setDegreesOfFreedom(final double dof) {
    ArgumentChecker.notNegativeOrZero(dof, "degrees of freedom");
    _dof = dof;
    _studentT = new StudentTDistribution(dof);
    setMultiplier();
  }

  private void setMultiplier() {
    _mult = Math.sqrt((_dof - 2) * getHorizon() / _dof / getPeriods()) * _studentT.getInverseCDF(getQuantile());
    _scale = getHorizon() / getPeriods();
  }

  @Override
  public Double evaluate(final NormalStatistics<?> statistics) {
    Validate.notNull(statistics, "statistics");
    return _mult * statistics.getStandardDeviation() - _scale * statistics.getMean();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_dof);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_mult);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_scale);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_studentT == null) ? 0 : _studentT.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    StudentTLinearVaRCalculator other = (StudentTLinearVaRCalculator) obj;
    if (Double.doubleToLongBits(_dof) != Double.doubleToLongBits(other._dof)) {
      return false;
    }
    if (Double.doubleToLongBits(_mult) != Double.doubleToLongBits(other._mult)) {
      return false;
    }
    if (Double.doubleToLongBits(_scale) != Double.doubleToLongBits(other._scale)) {
      return false;
    }
    if (_studentT == null) {
      if (other._studentT != null) {
        return false;
      }
    } else if (!_studentT.equals(other._studentT)) {
      return false;
    }
    return true;
  }

}
