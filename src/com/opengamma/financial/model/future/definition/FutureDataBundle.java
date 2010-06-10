/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * @author emcleod
 *
 */
public abstract class FutureDataBundle {
  private final YieldAndDiscountCurve _discountCurve;
  private final double _spot;
  private final ZonedDateTime _date;

  public FutureDataBundle(final YieldAndDiscountCurve discountCurve, final double spot, final ZonedDateTime date) {
    if (discountCurve == null)
      throw new IllegalArgumentException("Discount curve was null");
    if (spot < 0)
      throw new IllegalArgumentException("Spot was negative");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    _discountCurve = discountCurve;
    _spot = spot;
    _date = date;
  }

  public YieldAndDiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  public double getSpot() {
    return _spot;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public abstract FutureDataBundle withSpot(double newSpot);

  public abstract FutureDataBundle withDate(ZonedDateTime newDate);

  public abstract FutureDataBundle withDiscountCurve(YieldAndDiscountCurve newCurve);

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_discountCurve == null) ? 0 : _discountCurve.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_spot);
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
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final FutureDataBundle other = (FutureDataBundle) obj;
    if (_date == null) {
      if (other._date != null)
        return false;
    } else if (!_date.equals(other._date))
      return false;
    if (_discountCurve == null) {
      if (other._discountCurve != null)
        return false;
    } else if (!_discountCurve.equals(other._discountCurve))
      return false;
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot))
      return false;
    return true;
  }

}
