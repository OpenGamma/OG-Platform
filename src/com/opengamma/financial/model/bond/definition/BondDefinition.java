/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond.definition;

import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;

/**
 *
 */
public class BondDefinition {
  // TODO can this just be DoubleTimeSeries<?> ?
  private final YearOffsetDoubleTimeSeries _cashFlows;
  private final int _paymentsPerYear;
  private final int _daysInPeriod;
  private final int _daysInYear;

  public BondDefinition(final YearOffsetDoubleTimeSeries cashFlows, final int paymentsPerYear, final int daysInPeriod,
      final int daysInYear) {
    _cashFlows = cashFlows;
    _paymentsPerYear = paymentsPerYear;
    _daysInPeriod = daysInPeriod;
    _daysInYear = daysInYear;
  }

  public YearOffsetDoubleTimeSeries getCashFlows() {
    return _cashFlows;
  }

  public int getPaymentsPerYear() {
    return _paymentsPerYear;
  }

  public int getDaysInPeriod() {
    return _daysInPeriod;
  }

  public int getDaysInYear() {
    return _daysInYear;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_cashFlows == null) ? 0 : _cashFlows.hashCode());
    result = prime * result + _daysInPeriod;
    result = prime * result + _daysInYear;
    result = prime * result + _paymentsPerYear;
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final BondDefinition other = (BondDefinition) obj;
    if (_cashFlows == null) {
      if (other._cashFlows != null) {
        return false;
      }
    } else if (!_cashFlows.equals(other._cashFlows)) {
      return false;
    }
    if (_daysInPeriod != other._daysInPeriod) {
      return false;
    }
    if (_daysInYear != other._daysInYear) {
      return false;
    }
    if (_paymentsPerYear != other._paymentsPerYear) {
      return false;
    }
    return true;
  }
}
