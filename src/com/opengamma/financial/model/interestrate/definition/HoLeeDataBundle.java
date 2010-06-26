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
public class HoLeeDataBundle {
  private final ZonedDateTime _date;
  private final YieldAndDiscountCurve _yieldCurve;
  private final VolatilityCurve _volatilityCurve;

  public HoLeeDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve, final ZonedDateTime date) {
    Validate.notNull(yieldCurve);
    Validate.notNull(volatilityCurve);
    Validate.notNull(date);
    _yieldCurve = yieldCurve;
    _volatilityCurve = volatilityCurve;
    _date = date;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public YieldAndDiscountCurve getYieldCurve() {
    return _yieldCurve;
  }

  public VolatilityCurve getVolatilityCurve() {
    return _volatilityCurve;
  }

  public Double getInterestRate(final Double t) {
    return getYieldCurve().getInterestRate(t);
  }

  public Double getVolatility(final Double t) {
    return getVolatilityCurve().getVolatility(t);
  }
}
