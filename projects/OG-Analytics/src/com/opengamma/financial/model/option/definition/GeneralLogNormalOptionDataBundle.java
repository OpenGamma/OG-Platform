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
 * 
 */
public class GeneralLogNormalOptionDataBundle extends StandardOptionDataBundle {

  private final DriftSurface _drift;

  /**
   * Creates a data bundle for the SDE df/f = mu(f,t)dt + sigma(f,t)dw  
   * @param discountCurve 
   * @param localDrift The function mu(f,t)
   * @param localVolatility The function sigma(f,t)
   * @param spot Time zero value of f 
   * @param date Date created 
   */
  public GeneralLogNormalOptionDataBundle(final YieldAndDiscountCurve discountCurve, final DriftSurface localDrift, final VolatilitySurface localVolatility, final double spot, final ZonedDateTime date) {
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

  public GeneralLogNormalOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new GeneralLogNormalOptionDataBundle(curve, getDriftSurface(), getVolatilitySurface(), getSpot(), getDate());
  }

  public GeneralLogNormalOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), surface, getSpot(), getDate());
  }

  public GeneralLogNormalOptionDataBundle withDate(final ZonedDateTime date) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), getSpot(), date);
  }

  public GeneralLogNormalOptionDataBundle withSpot(final double spot) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), spot, getDate());
  }

  public GeneralLogNormalOptionDataBundle withDriftSurface(final DriftSurface localDrift) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), localDrift, getVolatilitySurface(), getSpot(), getDate());
  }
}
