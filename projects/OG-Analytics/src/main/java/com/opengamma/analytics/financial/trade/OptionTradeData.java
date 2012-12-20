/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.trade;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class OptionTradeData {
  private final double _numberOfContracts;
  private final double _pointValue;

  public OptionTradeData(final double numberOfContracts, final double pointValue) {
    Validate.isTrue(pointValue > 0);
    _numberOfContracts = numberOfContracts;
    _pointValue = pointValue;
  }

  public double getNumberOfContracts() {
    return _numberOfContracts;
  }

  public double getPointValue() {
    return _pointValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_numberOfContracts);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_pointValue);
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
    final OptionTradeData other = (OptionTradeData) obj;
    if (Double.doubleToLongBits(_numberOfContracts) != Double.doubleToLongBits(other._numberOfContracts)) {
      return false;
    }
    if (Double.doubleToLongBits(_pointValue) != Double.doubleToLongBits(other._pointValue)) {
      return false;
    }
    return true;
  }

}
