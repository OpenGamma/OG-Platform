/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;

/**
 * @author emcleod
 *
 */
public class ContinuousYieldForwardDataBundle extends ForwardDataBundle {
  private final double _yield;

  public ContinuousYieldForwardDataBundle(final double yield, final DiscountCurve discountCurve, final double spot, final ZonedDateTime date) {
    super(discountCurve, spot, date);
    _yield = yield;
  }

  public double getYield() {
    return _yield;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.model.forward.definition.ForwardDataBundle#withDate(javax.time.calendar.ZonedDateTime)
   */
  @Override
  public ForwardDataBundle withDate(final ZonedDateTime newDate) {
    return new ContinuousYieldForwardDataBundle(getYield(), getDiscountCurve(), getSpot(), newDate);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.model.forward.definition.ForwardDataBundle#withDiscountCurve(com.opengamma.financial.model
   * .interestrate.curve.DiscountCurve)
   */
  @Override
  public ForwardDataBundle withDiscountCurve(final DiscountCurve newCurve) {
    return new ContinuousYieldForwardDataBundle(getYield(), newCurve, getSpot(), getDate());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.model.forward.definition.ForwardDataBundle#withSpot(double)
   */
  @Override
  public ForwardDataBundle withSpot(final double newSpot) {
    return new ContinuousYieldForwardDataBundle(getYield(), getDiscountCurve(), newSpot, getDate());
  }

  public ForwardDataBundle withYield(final double newYield) {
    return new ContinuousYieldForwardDataBundle(newYield, getDiscountCurve(), getSpot(), getDate());
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
    final ContinuousYieldForwardDataBundle other = (ContinuousYieldForwardDataBundle) obj;
    if (Double.doubleToLongBits(_yield) != Double.doubleToLongBits(other._yield))
      return false;
    return true;
  }

}
