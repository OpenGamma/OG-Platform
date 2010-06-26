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

  public HullWhiteTwoFactorInterestRateDataBundle(final YieldAndDiscountCurve yieldCurve, final VolatilityCurve volatilityCurve1,
      final VolatilityCurve volatilityCurve2, final ZonedDateTime date, final double speed1, final double speed2, final double u, final double f,
      final double rho) {
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

  public Double getSpeed1() {
    return _speed1;
  }

  public Double getSpeed2() {
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

  public Double getVolatility1(final double t) {
    return _volatilityCurve1.getVolatility(t);
  }

  public Double getVolatility2(final double t) {
    return _volatilityCurve2.getVolatility(t);
  }

  public Double getRho() {
    return _rho;
  }
}
