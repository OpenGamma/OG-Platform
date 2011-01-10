/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FXForwardDataBundle extends ForwardDataBundle {
  private final YieldAndDiscountCurve _foreignCurve;

  public FXForwardDataBundle(final YieldAndDiscountCurve domesticCurve, final YieldAndDiscountCurve foreignCurve, final double spot, final ZonedDateTime date) {
    super(domesticCurve, spot, date);
    Validate.notNull(foreignCurve, "foreign curve");
    _foreignCurve = foreignCurve;
  }

  public YieldAndDiscountCurve getForeignCurve() {
    return _foreignCurve;
  }

  @Override
  public FXForwardDataBundle withDiscountCurve(final YieldAndDiscountCurve newCurve) {
    Validate.notNull(newCurve, "new curve");
    return new FXForwardDataBundle(newCurve, getForeignCurve(), getSpot(), getDate());
  }

  public FXForwardDataBundle withForeignCurve(final YieldAndDiscountCurve newCurve) {
    Validate.notNull(newCurve, "new curve");
    return new FXForwardDataBundle(getDiscountCurve(), newCurve, getSpot(), getDate());
  }

  @Override
  public FXForwardDataBundle withSpot(final double newSpot) {
    ArgumentChecker.notNegative(newSpot, "new spot");
    return new FXForwardDataBundle(getDiscountCurve(), getForeignCurve(), newSpot, getDate());
  }

  @Override
  public FXForwardDataBundle withDate(final ZonedDateTime newDate) {
    Validate.notNull(newDate, "new date");
    return new FXForwardDataBundle(getDiscountCurve(), getForeignCurve(), getSpot(), newDate);
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
    final FXForwardDataBundle other = (FXForwardDataBundle) obj;
    return ObjectUtils.equals(_foreignCurve, other._foreignCurve);
  }
}
