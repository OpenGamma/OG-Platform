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
public class FXForwardDataBundle extends ForwardDataBundle {
  private final DiscountCurve _foreignCurve;

  public FXForwardDataBundle(final DiscountCurve domesticCurve, final DiscountCurve foreignCurve, final double spot, final ZonedDateTime date) {
    super(domesticCurve, spot, date);
    _foreignCurve = foreignCurve;
  }

  public DiscountCurve getForeignCurve() {
    return _foreignCurve;
  }

  @Override
  public FXForwardDataBundle withDiscountCurve(final DiscountCurve newCurve) {
    return new FXForwardDataBundle(newCurve, getForeignCurve(), getSpot(), getDate());
  }

  public FXForwardDataBundle withForeignCurve(final DiscountCurve newCurve) {
    return new FXForwardDataBundle(getDiscountCurve(), newCurve, getSpot(), getDate());
  }

  @Override
  public FXForwardDataBundle withSpot(final double newSpot) {
    return new FXForwardDataBundle(getDiscountCurve(), getForeignCurve(), newSpot, getDate());
  }

  @Override
  public FXForwardDataBundle withDate(final ZonedDateTime newDate) {
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
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final FXForwardDataBundle other = (FXForwardDataBundle) obj;
    if (_foreignCurve == null) {
      if (other._foreignCurve != null)
        return false;
    } else if (!_foreignCurve.equals(other._foreignCurve))
      return false;
    return true;
  }
}
