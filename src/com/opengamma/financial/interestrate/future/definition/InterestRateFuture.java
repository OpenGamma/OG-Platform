/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFuture implements InterestRateDerivative {
  private final double _startTime;
  private final double _endTime;

  public InterestRateFuture(final double startTime, final double endTime) {
    ArgumentChecker.notNegative(startTime, "start time");
    ArgumentChecker.notNegative(endTime, "end time");
    if (startTime >= endTime) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    _startTime = startTime;
    _endTime = endTime;
  }

  public double getStartTime() {
    return _startTime;
  }

  public double getEndTime() {
    return _endTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_startTime);
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
    final InterestRateFuture other = (InterestRateFuture) obj;
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_startTime) != Double.doubleToLongBits(other._startTime)) {
      return false;
    }
    return true;
  }

}
