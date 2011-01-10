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
public abstract class ForwardDataBundle {
  private final YieldAndDiscountCurve _discountCurve;
  private final double _spot;
  private final ZonedDateTime _date;

  public ForwardDataBundle(final YieldAndDiscountCurve discountCurve, final double spot, final ZonedDateTime date) {
    Validate.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNegative(spot, "spot");
    Validate.notNull(date, "date");
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

  public abstract ForwardDataBundle withSpot(double newSpot);

  public abstract ForwardDataBundle withDate(ZonedDateTime newDate);

  public abstract ForwardDataBundle withDiscountCurve(YieldAndDiscountCurve newCurve);

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
    final ForwardDataBundle other = (ForwardDataBundle) obj;
    if (!ObjectUtils.equals(_date, other._date) || !ObjectUtils.equals(_discountCurve, other._discountCurve)) {
      return false;
    }
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot)) {
      return false;
    }
    return true;
  }
}
