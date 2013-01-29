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
public class StandardOptionDataBundle extends OptionDataBundle {
  private final double _b;
  private final double _spot;

  // TODO need a cost of carry model
  public StandardOptionDataBundle(final YieldAndDiscountCurve interestRateCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date) {
    super(interestRateCurve, volatilitySurface, date);
    _b = b;
    _spot = spot;
  }

  public StandardOptionDataBundle(final StandardOptionDataBundle data) {
    super(data);
    _b = data.getCostOfCarry();
    _spot = data.getSpot();
  }

  public double getCostOfCarry() {
    return _b;
  }

  public double getSpot() {
    return _spot;
  }

  @Override
  public StandardOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new StandardOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate());
  }

  public StandardOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new StandardOptionDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate());
  }

  @Override
  public StandardOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate());
  }

  @Override
  public StandardOptionDataBundle withDate(final ZonedDateTime date) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date);
  }

  public StandardOptionDataBundle withSpot(final double spot) {
    return new StandardOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_b);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spot);
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
    final StandardOptionDataBundle other = (StandardOptionDataBundle) obj;
    if (Double.doubleToLongBits(_b) != Double.doubleToLongBits(other._b)) {
      return false;
    }
    return Double.doubleToLongBits(_spot) == Double.doubleToLongBits(other._spot);
  }
}
