/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 */
public class BlackDermanToyDataBundle {
  private final YieldAndDiscountCurve _yieldCurve;
  private final VolatilityCurve _volatilityCurve;
  private final ZonedDateTime _date;

  public BlackDermanToyDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve, final ZonedDateTime date) {
    Validate.notNull(yieldCurve);
    Validate.notNull(volatilityCurve);
    Validate.notNull(date);
    _yieldCurve = yieldCurve;
    _volatilityCurve = volatilityCurve;
    _date = date;
  }

  public YieldAndDiscountCurve getYieldCurve() {
    return _yieldCurve;
  }

  public VolatilityCurve getVolatilityCurve() {
    return _volatilityCurve;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public Double getInterestRate(final Double t) {
    return getYieldCurve().getInterestRate(t);
  }

  public Double getVolatility(final Double t) {
    return getVolatilityCurve().getVolatility(t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
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
    BlackDermanToyDataBundle other = (BlackDermanToyDataBundle) obj;
    return ObjectUtils.equals(_date, other._date) && ObjectUtils.equals(_volatilityCurve, other._volatilityCurve) && ObjectUtils.equals(_yieldCurve, other._yieldCurve);
  }

}
