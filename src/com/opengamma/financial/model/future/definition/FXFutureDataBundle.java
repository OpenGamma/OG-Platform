/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *
 */
public class FXFutureDataBundle extends FutureDataBundle {
  private final YieldAndDiscountCurve _foreignCurve;

  public FXFutureDataBundle(final YieldAndDiscountCurve domesticCurve, final YieldAndDiscountCurve foreignCurve, final double spot, final ZonedDateTime date) {
    super(domesticCurve, spot, date);
    Validate.notNull(foreignCurve, "Foreign curve must not be null.");
    _foreignCurve = foreignCurve;
  }

  public YieldAndDiscountCurve getForeignCurve() {
    return _foreignCurve;
  }

  @Override
  public FXFutureDataBundle withDiscountCurve(final YieldAndDiscountCurve newCurve) {
    Validate.notNull(newCurve, "New curve was null");
    return new FXFutureDataBundle(newCurve, getForeignCurve(), getSpot(), getDate());
  }

  public FXFutureDataBundle withForeignCurve(final YieldAndDiscountCurve newCurve) {
    Validate.notNull(newCurve, "New curve was null");
    return new FXFutureDataBundle(getDiscountCurve(), newCurve, getSpot(), getDate());
  }

  @Override
  public FXFutureDataBundle withSpot(final double newSpot) {
    Validate.isTrue(newSpot >= 0, "New spot was negative", newSpot);
    return new FXFutureDataBundle(getDiscountCurve(), getForeignCurve(), newSpot, getDate());
  }

  @Override
  public FXFutureDataBundle withDate(final ZonedDateTime newDate) {
    Validate.notNull(newDate, "New date was null");
    return new FXFutureDataBundle(getDiscountCurve(), getForeignCurve(), getSpot(), newDate);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_foreignCurve == null) ? 0 : _foreignCurve.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FXFutureDataBundle other = (FXFutureDataBundle) obj;
    if (_foreignCurve == null) {
      if (other._foreignCurve != null) {
        return false;
      }
    } else if (!_foreignCurve.equals(other._foreignCurve)) {
      return false;
    }
    return true;
  }

}
