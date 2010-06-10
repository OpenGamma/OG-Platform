/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class StandardOptionDataBundle {
  private final YieldAndDiscountCurve _discountCurve;
  private final Double _b;
  private final VolatilitySurface _volatilitySurface;
  private final Double _spot;
  private final ZonedDateTime _date;

  // TODO need a cost of carry model
  public StandardOptionDataBundle(final YieldAndDiscountCurve discountCurve, final Double b, final VolatilitySurface volatilitySurface, final Double spot, final ZonedDateTime date) {
    _discountCurve = discountCurve;
    _b = b;
    _volatilitySurface = volatilitySurface;
    _spot = spot;
    _date = date;
  }

  public StandardOptionDataBundle(final StandardOptionDataBundle data) {
    Validate.notNull(data);
    _discountCurve = data.getDiscountCurve();
    _b = data.getCostOfCarry();
    _volatilitySurface = data.getVolatilitySurface();
    _spot = data.getSpot();
    _date = data.getDate();
  }

  public Double getInterestRate(final Double t) {
    return getDiscountCurve().getInterestRate(t);
  }

  public Double getCostOfCarry() {
    return _b;
  }

  public Double getVolatility(final Double timeToExpiry, final Double strike) {
    return getVolatilitySurface().getVolatility(Pair.of(timeToExpiry, strike));
  }

  public Double getSpot() {
    return _spot;
  }

  public YieldAndDiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public StandardOptionDataBundle withDiscountCurve(final YieldAndDiscountCurve curve) {
    return new StandardOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate());
  }

  public StandardOptionDataBundle withCostOfCarry(final Double costOfCarry) {
    return new StandardOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate());
  }

  public StandardOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new StandardOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate());
  }

  public StandardOptionDataBundle withDate(final ZonedDateTime date) {
    return new StandardOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date);
  }

  public StandardOptionDataBundle withSpot(final Double spot) {
    return new StandardOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_b == null ? 0 : _b.hashCode());
    result = prime * result + (_date == null ? 0 : _date.hashCode());
    result = prime * result + (_discountCurve == null ? 0 : _discountCurve.hashCode());
    result = prime * result + (_spot == null ? 0 : _spot.hashCode());
    result = prime * result + (_volatilitySurface == null ? 0 : _volatilitySurface.hashCode());
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
    if (_b == null) {
      if (other._b != null) {
        return false;
      }
    } else if (!_b.equals(other._b)) {
      return false;
    }
    if (_date == null) {
      if (other._date != null) {
        return false;
      }
    } else if (!_date.equals(other._date)) {
      return false;
    }
    if (_discountCurve == null) {
      if (other._discountCurve != null) {
        return false;
      }
    } else if (!_discountCurve.equals(other._discountCurve)) {
      return false;
    }
    if (_spot == null) {
      if (other._spot != null) {
        return false;
      }
    } else if (!_spot.equals(other._spot)) {
      return false;
    }
    if (_volatilitySurface == null) {
      if (other._volatilitySurface != null) {
        return false;
      }
    } else if (!_volatilitySurface.equals(other._volatilitySurface)) {
      return false;
    }
    return true;
  }
}
