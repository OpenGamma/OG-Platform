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

  public HullWhiteOneFactorInterestRateDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve, final ZonedDateTime date,
      final double speed) {
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
}
