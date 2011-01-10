/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class StandardOptionDataBundle {
  private final YieldAndDiscountCurve _interestRateCurve;
  private final double _b;
  private final VolatilitySurface _volatilitySurface;
  private final double _spot;
  private final ZonedDateTime _date;

  // TODO need a cost of carry model
  public StandardOptionDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date) {
    _interestRateCurve = discountCurve;
    _b = b;
    _volatilitySurface = volatilitySurface;
    _spot = spot;
    _date = date;
  }

  public StandardOptionDataBundle(final StandardOptionDataBundle data) {
    Validate.notNull(data);
    _interestRateCurve = data.getInterestRateCurve();
    _b = data.getCostOfCarry();
    _volatilitySurface = data.getVolatilitySurface();
    _spot = data.getSpot();
    _date = data.getDate();
  }

  public double getInterestRate(final double t) {
    return getInterestRateCurve().getInterestRate(t);
  }

  public double getCostOfCarry() {
    return _b;
  }

  public double getVolatility(final double timeToExpiry, final double strike) {
    return getVolatilitySurface().getVolatility(DoublesPair.of(timeToExpiry, strike));
  }

  public double getSpot() {
    return _spot;
  }

  public YieldAndDiscountCurve getInterestRateCurve() {
    return _interestRateCurve;
  }

  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public StandardOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new StandardOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate());
  }

  public StandardOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new StandardOptionDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate());
  }

  public StandardOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate());
  }

  public StandardOptionDataBundle withDate(final ZonedDateTime date) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date);
  }

  public StandardOptionDataBundle withSpot(final double spot) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_b);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_interestRateCurve == null) ? 0 : _interestRateCurve.hashCode());
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final StandardOptionDataBundle other = (StandardOptionDataBundle) obj;
    if (Double.doubleToLongBits(_b) != Double.doubleToLongBits(other._b)) {
      return false;
    }
    if (!(ObjectUtils.equals(_date, other._date))) {
      return false;
    }
    if (!(ObjectUtils.equals(_interestRateCurve, other._interestRateCurve))) {
      return false;
    }
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot)) {
      return false;
    }
    if (!(ObjectUtils.equals(_volatilitySurface, other._volatilitySurface))) {
      return false;
    }
    return true;
  }
}
