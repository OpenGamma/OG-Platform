/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class BlackOptionDataBundle extends OptionDataBundle {
  private final double _forward;

  public BlackOptionDataBundle(final double forward, final YieldAndDiscountCurve interestRateCurve, final VolatilitySurface volatilitySurface, final ZonedDateTime date) {
    super(interestRateCurve, volatilitySurface, date);
    _forward = forward;
  }

  public BlackOptionDataBundle(final BlackOptionDataBundle data) {
    super(data);
    _forward = data.getForward();
  }

  public double getForward() {
    return _forward;
  }

  public double getDiscountFactor(final double t) {
    return getInterestRateCurve().getDiscountFactor(t);
  }

  @Override
  public BlackOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new BlackOptionDataBundle(getForward(), curve, getVolatilitySurface(), getDate());
  }

  public BlackOptionDataBundle withForward(final double forward) {
    return new BlackOptionDataBundle(forward, getInterestRateCurve(), getVolatilitySurface(), getDate());
  }

  @Override
  public BlackOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new BlackOptionDataBundle(getForward(), getInterestRateCurve(), surface, getDate());
  }

  @Override
  public BlackOptionDataBundle withDate(final ZonedDateTime date) {
    return new BlackOptionDataBundle(getForward(), getInterestRateCurve(), getVolatilitySurface(), date);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final BlackOptionDataBundle other = (BlackOptionDataBundle) obj;
    return Double.doubleToLongBits(_forward) == Double.doubleToLongBits(other._forward);
  }

}
