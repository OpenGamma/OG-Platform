/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class OptionDataBundle {
  private final YieldAndDiscountCurve _interestRateCurve;
  private final VolatilitySurface _volatilitySurface;
  private final ZonedDateTime _date;

  public OptionDataBundle(final YieldAndDiscountCurve interestRateCurve, final VolatilitySurface volatilitySurface, final ZonedDateTime date) {
    Validate.notNull(date, "date");
    _interestRateCurve = interestRateCurve;
    _volatilitySurface = volatilitySurface;
    _date = date;
  }

  public OptionDataBundle(final OptionDataBundle data) {
    Validate.notNull(data);
    _interestRateCurve = data.getInterestRateCurve();
    _volatilitySurface = data.getVolatilitySurface();
    _date = data.getDate();
  }

  public double getInterestRate(final double t) {
    return getInterestRateCurve().getInterestRate(t);
  }

  public YieldAndDiscountCurve getInterestRateCurve() {
    return _interestRateCurve;
  }

  public double getVolatility(final double timeToExpiry, final double strike) {
    return getVolatilitySurface().getVolatility(DoublesPair.of(timeToExpiry, strike));
  }

  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public OptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new OptionDataBundle(curve, getVolatilitySurface(), getDate());
  }

  public OptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new OptionDataBundle(getInterestRateCurve(), surface, getDate());
  }

  public OptionDataBundle withDate(final ZonedDateTime date) {
    return new OptionDataBundle(getInterestRateCurve(), getVolatilitySurface(), date);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _date.hashCode();
    result = prime * result + ((_interestRateCurve == null) ? 0 : _interestRateCurve.hashCode());
    result = prime * result + ((_volatilitySurface == null) ? 0 : _volatilitySurface.hashCode());
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
    final OptionDataBundle other = (OptionDataBundle) obj;
    if (!ObjectUtils.equals(_volatilitySurface, other._volatilitySurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_interestRateCurve, other._interestRateCurve)) {
      return false;
    }
    return ObjectUtils.equals(_date, other._date);
  }

}
