/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;

/**
 * @author emcleod
 *
 */
public class StandardFutureDataBundle extends FutureDataBundle {
  private final double _yield;
  private final double _storageCost;

  public StandardFutureDataBundle(final DiscountCurve discountCurve, final double spot, final ZonedDateTime date, final double yield, final double storageCost) {
    super(discountCurve, spot, date);
    _yield = yield;
    _storageCost = storageCost;
  }

  public double getYield() {
    return _yield;
  }

  public double getStorageCost() {
    return _storageCost;
  }

  public FutureDataBundle withyield(final double newYield) {
    return new StandardFutureDataBundle(getDiscountCurve(), getSpot(), getDate(), newYield, getStorageCost());
  }

  @Override
  public FutureDataBundle withDate(final ZonedDateTime newDate) {
    return new StandardFutureDataBundle(getDiscountCurve(), getSpot(), newDate, getYield(), getStorageCost());
  }

  @Override
  public FutureDataBundle withDiscountCurve(final DiscountCurve newCurve) {
    return new StandardFutureDataBundle(newCurve, getSpot(), getDate(), getYield(), getStorageCost());
  }

  @Override
  public FutureDataBundle withSpot(final double newSpot) {
    return new StandardFutureDataBundle(getDiscountCurve(), newSpot, getDate(), getYield(), getStorageCost());
  }

  public FutureDataBundle withStorageCost(final double newStorageCost) {
    return new StandardFutureDataBundle(getDiscountCurve(), getSpot(), getDate(), getYield(), newStorageCost);
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
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StandardFutureDataBundle other = (StandardFutureDataBundle) obj;
    if (Double.doubleToLongBits(_storageCost) != Double.doubleToLongBits(other._storageCost))
      return false;
    if (Double.doubleToLongBits(_yield) != Double.doubleToLongBits(other._yield))
      return false;
    return true;
  }

}
