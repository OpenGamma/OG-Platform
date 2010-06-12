/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * @author emcleod
 *
 */
public class FXForwardDataBundle extends ForwardDataBundle {
  private final YieldAndDiscountCurve _foreignCurve;

  public FXForwardDataBundle(final YieldAndDiscountCurve domesticCurve, final YieldAndDiscountCurve foreignCurve, final double spot, final ZonedDateTime date) {
    super(domesticCurve, spot, date);
    if (foreignCurve == null) {
      throw new IllegalArgumentException("Foreign curve was null");
    }
    _foreignCurve = foreignCurve;
  }

  public YieldAndDiscountCurve getForeignCurve() {
    return _foreignCurve;
  }

  @Override
  public FXForwardDataBundle withDiscountCurve(final YieldAndDiscountCurve newCurve) {
    if (newCurve == null) {
      throw new IllegalArgumentException("New curve was null");
    }
    return new FXForwardDataBundle(newCurve, getForeignCurve(), getSpot(), getDate());
  }

  public FXForwardDataBundle withForeignCurve(final YieldAndDiscountCurve newCurve) {
    if (newCurve == null) {
      throw new IllegalArgumentException("New curve was null");
    }
    return new FXForwardDataBundle(getDiscountCurve(), newCurve, getSpot(), getDate());
  }

  @Override
  public FXForwardDataBundle withSpot(final double newSpot) {
    if (newSpot < 0) {
      throw new IllegalArgumentException("New spot was negative");
    }
    return new FXForwardDataBundle(getDiscountCurve(), getForeignCurve(), newSpot, getDate());
  }

  @Override
  public FXForwardDataBundle withDate(final ZonedDateTime newDate) {
    if (newDate == null) {
      throw new IllegalArgumentException("New date was null");
    }
    return new FXForwardDataBundle(getDiscountCurve(), getForeignCurve(), getSpot(), newDate);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_foreignCurve == null) ? 0 : _foreignCurve.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final FXForwardDataBundle other = (FXForwardDataBundle) obj;
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
