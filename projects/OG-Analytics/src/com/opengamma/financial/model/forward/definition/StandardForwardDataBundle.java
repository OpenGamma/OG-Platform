/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *
 */
public class StandardForwardDataBundle extends ForwardDataBundle {
  private final double _yield;
  private final double _storageCost;

  public StandardForwardDataBundle(final double yield, final YieldAndDiscountCurve discountCurve, final double spot, final ZonedDateTime date, final double storageCost) {
    super(discountCurve, spot, date);
    Validate.isTrue(storageCost >= 0, "Storage cost cannot be negative", storageCost);
    _yield = yield;
    _storageCost = storageCost;
  }

  public double getYield() {
    return _yield;
  }

  public double getStorageCost() {
    return _storageCost;
  }

  @Override
  public ForwardDataBundle withDate(final ZonedDateTime newDate) {
    Validate.notNull(newDate, "new date");
    return new StandardForwardDataBundle(getYield(), getDiscountCurve(), getSpot(), newDate, getStorageCost());
  }

  @Override
  public ForwardDataBundle withDiscountCurve(final YieldAndDiscountCurve newCurve) {
    Validate.notNull(newCurve, "New curve was null");
    return new StandardForwardDataBundle(getYield(), newCurve, getSpot(), getDate(), getStorageCost());
  }

  @Override
  public ForwardDataBundle withSpot(final double newSpot) {
    Validate.isTrue(newSpot >= 0, "New spot was negative");
    return new StandardForwardDataBundle(getYield(), getDiscountCurve(), newSpot, getDate(), getStorageCost());
  }

  public ForwardDataBundle withStorageCost(final double newStorageCost) {
    Validate.isTrue(newStorageCost >= 0, "New storage cost was negative");
    return new StandardForwardDataBundle(getYield(), getDiscountCurve(), getSpot(), getDate(), newStorageCost);
  }

  public ForwardDataBundle withYield(final double newYield) {
    return new StandardForwardDataBundle(newYield, getDiscountCurve(), getSpot(), getDate(), getStorageCost());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_storageCost);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yield);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final StandardForwardDataBundle other = (StandardForwardDataBundle) obj;
    if (Double.doubleToLongBits(_storageCost) != Double.doubleToLongBits(other._storageCost)) {
      return false;
    }
    if (Double.doubleToLongBits(_yield) != Double.doubleToLongBits(other._yield)) {
      return false;
    }
    return true;
  }

}
