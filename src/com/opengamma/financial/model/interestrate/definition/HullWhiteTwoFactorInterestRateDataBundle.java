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
public class HullWhiteTwoFactorInterestRateDataBundle {
  private final ZonedDateTime _date;
  private final YieldAndDiscountCurve _yieldCurve;
  private final double _speed1;
  private final double _speed2;
  private final double _u;
  private final double _f;
  private final VolatilityCurve _volatilityCurve1;
  private final VolatilityCurve _volatilityCurve2;
  private final double _rho;

  public HullWhiteTwoFactorInterestRateDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve1, final VolatilityCurve volatilityCurve2, final ZonedDateTime date,
      final double speed1, final double speed2, final double u, final double f, final double rho) {
    Validate.notNull(yieldCurve);
    Validate.notNull(volatilityCurve1);
    Validate.notNull(volatilityCurve2);
    Validate.notNull(date);
    _date = date;
    _yieldCurve = yieldCurve;
    _speed1 = speed1;
    _speed2 = speed2;
    _u = u;
    _f = f;
    _volatilityCurve1 = volatilityCurve1;
    _volatilityCurve2 = volatilityCurve2;
    _rho = rho;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public Double getInterestRate(final double t) {
    return _yieldCurve.getInterestRate(t);
  }

  public Double getFirstSpeed() {
    return _speed1;
  }

  public Double getSecondSpeed() {
    return _speed2;
  }

  // TODO
  public Double getU() {
    return _u;
  }

  // TODO
  public Double getF() {
    return _f;
  }

  public Double getFirstVolatility(final double t) {
    return _volatilityCurve1.getVolatility(t);
  }

  public Double getSecondVolatility(final double t) {
    return _volatilityCurve2.getVolatility(t);
  }

  public Double getRho() {
    return _rho;
  }

  public YieldAndDiscountCurve getYieldCurve() {
    return _yieldCurve;
  }

  public VolatilityCurve getFirstVolatilityCurve() {
    return _volatilityCurve1;
  }

  public VolatilityCurve getSecondVolatilityCurve() {
    return _volatilityCurve2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_f);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_speed1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_speed2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_u);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilityCurve1 == null) ? 0 : _volatilityCurve1.hashCode());
    result = prime * result + ((_volatilityCurve2 == null) ? 0 : _volatilityCurve2.hashCode());
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
    HullWhiteTwoFactorInterestRateDataBundle other = (HullWhiteTwoFactorInterestRateDataBundle) obj;
    if (_date == null) {
      if (other._date != null) {
        return false;
      }
    } else if (!_date.equals(other._date)) {
      return false;
    }
    if (Double.doubleToLongBits(_f) != Double.doubleToLongBits(other._f)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_speed1) != Double.doubleToLongBits(other._speed1)) {
      return false;
    }
    if (Double.doubleToLongBits(_speed2) != Double.doubleToLongBits(other._speed2)) {
      return false;
    }
    if (Double.doubleToLongBits(_u) != Double.doubleToLongBits(other._u)) {
      return false;
    }
    if (_volatilityCurve1 == null) {
      if (other._volatilityCurve1 != null) {
        return false;
      }
    } else if (!_volatilityCurve1.equals(other._volatilityCurve1)) {
      return false;
    }
    if (_volatilityCurve2 == null) {
      if (other._volatilityCurve2 != null) {
        return false;
      }
    } else if (!_volatilityCurve2.equals(other._volatilityCurve2)) {
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
