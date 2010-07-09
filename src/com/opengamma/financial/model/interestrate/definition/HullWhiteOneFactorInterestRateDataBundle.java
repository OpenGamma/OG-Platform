/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 */
public class HullWhiteOneFactorInterestRateDataBundle {
  private final ZonedDateTime _date;
  private final YieldAndDiscountCurve _yieldCurve;
  private final double _speed;
  private final VolatilityCurve _volatilityCurve;

  public HullWhiteOneFactorInterestRateDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve, final ZonedDateTime date, final double speed) {
    Validate.notNull(yieldCurve);
    Validate.notNull(volatilityCurve);
    Validate.notNull(date);
    _date = date;
    _yieldCurve = yieldCurve;
    _speed = speed;
    _volatilityCurve = volatilityCurve;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public Double getInterestRate(final Double t) {
    return _yieldCurve.getInterestRate(t);
  }

  public double getSpeed() {
    return _speed;
  }

  public Double getVolatility(final Double t) {
    return _volatilityCurve.getVolatility(t);
  }

  public YieldAndDiscountCurve getYieldCurve() {
    return _yieldCurve;
  }

  public VolatilityCurve getVolatilityCurve() {
    return _volatilityCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_speed);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilityCurve == null) ? 0 : _volatilityCurve.hashCode());
    result = prime * result + ((_yieldCurve == null) ? 0 : _yieldCurve.hashCode());
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
    HullWhiteOneFactorInterestRateDataBundle other = (HullWhiteOneFactorInterestRateDataBundle) obj;
    if (_date == null) {
      if (other._date != null) {
        return false;
      }
    } else if (!_date.equals(other._date)) {
      return false;
    }
    if (Double.doubleToLongBits(_speed) != Double.doubleToLongBits(other._speed)) {
      return false;
    }
    if (_volatilityCurve == null) {
      if (other._volatilityCurve != null) {
        return false;
      }
    } else if (!_volatilityCurve.equals(other._volatilityCurve)) {
      return false;
    }
    if (_yieldCurve == null) {
      if (other._yieldCurve != null) {
        return false;
      }
    } else if (!_yieldCurve.equals(other._yieldCurve)) {
      return false;
    }
    return true;
  }

}
