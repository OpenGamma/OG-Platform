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
 * 
 */
public class GeneralLogNormalOptionDataBundle extends StandardOptionDataBundle {
  private final DriftSurface _drift;

  /**
   * Creates a data bundle for the SDE df/f = mu(f,t)dt + sigma(f,t)dw  
   * @param discountCurve 
   * @param localDrift The function mu(f,t)
   * @param localVolatility The function sigma(f,t)
   * @param spot Time-zero value of f 
   * @param date Date created 
   */
  public GeneralLogNormalOptionDataBundle(final YieldAndDiscountCurve discountCurve, final DriftSurface localDrift, final VolatilitySurface localVolatility, final double spot, 
      final ZonedDateTime date) {
    super(discountCurve, 0.0, localVolatility, spot, date);
    Validate.notNull(localDrift, "null localDrift");
    _drift = localDrift;
  }

  public GeneralLogNormalOptionDataBundle(final GeneralLogNormalOptionDataBundle data) {
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
  public GeneralLogNormalOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new GeneralLogNormalOptionDataBundle(curve, getDriftSurface(), getVolatilitySurface(), getSpot(), getDate());
  }

  @Override
  public GeneralLogNormalOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), surface, getSpot(), getDate());
  }

  @Override
  public GeneralLogNormalOptionDataBundle withDate(final ZonedDateTime date) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), getSpot(), date);
  }

  @Override
  public GeneralLogNormalOptionDataBundle withSpot(final double spot) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), getDriftSurface(), getVolatilitySurface(), spot, getDate());
  }

  public GeneralLogNormalOptionDataBundle withDriftSurface(final DriftSurface localDrift) {
    return new GeneralLogNormalOptionDataBundle(getInterestRateCurve(), localDrift, getVolatilitySurface(), getSpot(), getDate());
  }

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
    final GeneralLogNormalOptionDataBundle other = (GeneralLogNormalOptionDataBundle) obj;
    return ObjectUtils.equals(_drift, other._drift);
  }
}
