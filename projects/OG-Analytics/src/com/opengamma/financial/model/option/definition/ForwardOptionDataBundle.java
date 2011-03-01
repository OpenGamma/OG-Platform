/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.DriftSurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * data bundle for the SDE df = a(f,t)dt + b(f,t)dw  
 */
public class ForwardOptionDataBundle extends StandardOptionDataBundle {

  private final DriftSurface _drift;

  /**
   * Creates a data bundle for the SDE df = a(f,t)dt + b(f,t)dw  
   * @param discountCurve 
   * @param localDrift The function a(f,t)
   * @param localVolatility The function b(f,t)
   * @param spot Time zero value of f 
   * @param date
   */
  public ForwardOptionDataBundle(final YieldAndDiscountCurve discountCurve, final DriftSurface localDrift, final VolatilitySurface localVolatility, final double spot, final ZonedDateTime date) {
    super(discountCurve, 0.0, localVolatility, spot, date);
    Validate.notNull(localDrift, "null localDrift");
    _drift = localDrift;
  }

  /**
   * Gets the drift field.
   * @return the drift
   */
  public DriftSurface getDriftSurface() {
    return _drift;
  }

  public double getLocalDrift(final double f, final double t) {
    return _drift.getDrift(f, t);
  }

  public double getLocalVolatility(final double f, final double t) {
    return getVolatility(t, f);
  }

  public ForwardOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new ForwardOptionDataBundle(curve, getDriftSurface(), getVolatilitySurface(), getSpot(), getDate());
  }

  public ForwardOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new ForwardOptionDataBundle(getInterestRateCurve(), getDriftSurface(), surface, getSpot(), getDate());
  }

  public ForwardOptionDataBundle withDate(final ZonedDateTime date) {
    return new ForwardOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), getSpot(), date);
  }

  public ForwardOptionDataBundle withSpot(final double spot) {
    return new ForwardOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), spot, getDate());
  }

  public ForwardOptionDataBundle withDriftSurface(final DriftSurface localDrift) {
    return new ForwardOptionDataBundle(getInterestRateCurve(), localDrift, getVolatilitySurface(), getSpot(), getDate());
  }

  //TODO finish this once we have the ability to multiply / divide surfaces by a constant amount
  /*public static ForwardOptionDataBundle fromNormalSurfaces(final DriftSurface localDrift, final VolatilitySurface localVolatility, double f) {
    
  }*/
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_drift == null) ? 0 : _drift.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForwardOptionDataBundle other = (ForwardOptionDataBundle) obj;
    if (_drift == null) {
      if (other._drift != null) {
        return false;
      }
    } else if (!_drift.equals(other._drift)) {
      return false;
    }
    return true;
  }

}
