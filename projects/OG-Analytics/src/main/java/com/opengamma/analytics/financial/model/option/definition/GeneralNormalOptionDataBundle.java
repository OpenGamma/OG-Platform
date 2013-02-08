/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.DriftSurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * data bundle for the SDE df = a(f,t)dt + b(f,t)dw  
 */
public class GeneralNormalOptionDataBundle extends StandardOptionDataBundle {
  private final DriftSurface _drift;

  /**
   * Creates a data bundle for the SDE df = a(f,t)dt + b(f,t)dw  
   * @param discountCurve 
   * @param localDrift The function a(f,t)
   * @param localVolatility The function b(f,t)
   * @param spot Time zero value of f 
   * @param date Date created 
   */
  public GeneralNormalOptionDataBundle(final YieldAndDiscountCurve discountCurve, final DriftSurface localDrift, final VolatilitySurface localVolatility, final double spot, final ZonedDateTime date) {
    super(discountCurve, 0.0, localVolatility, spot, date);
    Validate.notNull(localDrift, "null localDrift");
    _drift = localDrift;
  }

  public GeneralNormalOptionDataBundle(final GeneralNormalOptionDataBundle data) {
    super(data);
    _drift = data.getDriftSurface();
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

  @Override
  public GeneralNormalOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new GeneralNormalOptionDataBundle(curve, getDriftSurface(), getVolatilitySurface(), getSpot(), getDate());
  }

  @Override
  public GeneralNormalOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new GeneralNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), surface, getSpot(), getDate());
  }

  @Override
  public GeneralNormalOptionDataBundle withDate(final ZonedDateTime date) {
    return new GeneralNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), getSpot(), date);
  }

  @Override
  public GeneralNormalOptionDataBundle withSpot(final double spot) {
    return new GeneralNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), spot, getDate());
  }

  public GeneralNormalOptionDataBundle withDriftSurface(final DriftSurface localDrift) {
    return new GeneralNormalOptionDataBundle(getInterestRateCurve(), localDrift, getVolatilitySurface(), getSpot(), getDate());
  }

  //TODO finish this once we have the ability to multiply / divide surfaces by a constant amount
  /*public static ForwardOptionDataBundle fromNormalSurfaces(final DriftSurface localDrift, final VolatilitySurface localVolatility, double f) {
    
  }*/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _drift.hashCode();
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
    final GeneralNormalOptionDataBundle other = (GeneralNormalOptionDataBundle) obj;
    return ObjectUtils.equals(_drift, other._drift);
  }

}
