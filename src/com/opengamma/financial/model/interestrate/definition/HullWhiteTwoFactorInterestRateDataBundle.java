/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 * @author emcleod
 */
public class HullWhiteTwoFactorInterestRateDataBundle {
  private final ZonedDateTime _date;
  private final DiscountCurve _yieldCurve;
  private final Double _speed1;
  private final Double _speed2;
  private final Double _u;
  Double _f;
  private final VolatilityCurve _volatilityCurve1;
  private final VolatilityCurve _volatilityCurve2;
  private final Double _rho;

  public HullWhiteTwoFactorInterestRateDataBundle(final ZonedDateTime date, final DiscountCurve yieldCurve, final Double speed1, final Double speed2, final Double u,
      final Double f, final VolatilityCurve volatilityCurve1, final VolatilityCurve volatilityCurve2, final Double rho) {
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

  public Double getInterestRate(final Double t) {
    return _yieldCurve.getInterestRate(t);
  }

  public Double getSpeed1() {
    return _speed1;
  }

  public Double getSpeed2() {
    return _speed2;
  }

  // TODO
  public Double getU(final Double t) {
    return _u;
  }

  // TODO
  public Double getF(final Double r) {
    return _f;
  }

  public Double getVolatility1(final Double t) {
    return _volatilityCurve1.getVolatility(t);
  }

  public Double getVolatility2(final Double t) {
    return _volatilityCurve2.getVolatility(t);
  }

  public Double getRho() {
    return _rho;
  }
}
