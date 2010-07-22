/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFuture implements InterestRateDerivative {
  private final double _startTime;
  private final double _endTime;
  private final String _curveName;

  public InterestRateFuture(final double startTime, final double endTime, final String yieldCurveName) {
    ArgumentChecker.notNegative(startTime, "start time");
    ArgumentChecker.notNegative(endTime, "end time");
    Validate.notNull(yieldCurveName);
    if (startTime >= endTime) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    _curveName = yieldCurveName;
    _startTime = startTime;
    _endTime = endTime;
  }

  public double getStartTime() {
    return _startTime;
  }

  public double getEndTime() {
    return _endTime;
  }

  public String getCurveName() {
    return _curveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_startTime);
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
    InterestRateFuture other = (InterestRateFuture) obj;
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_startTime) != Double.doubleToLongBits(other._startTime)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitInterestRateFuture(this, curves);
  }

}
