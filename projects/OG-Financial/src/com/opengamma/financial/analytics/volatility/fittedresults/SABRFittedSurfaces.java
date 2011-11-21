/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.fittedresults;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SABRFittedSurfaces {
  private final VolatilitySurface _alphaSurface;
  private final VolatilitySurface _betaSurface;
  private final VolatilitySurface _nuSurface;
  private final VolatilitySurface _rhoSurface;
  private final Map<DoublesPair, DoubleMatrix2D> _inverseJacobian;
  private final Currency _currency;
  private final DayCount _dayCount;


  public SABRFittedSurfaces(final VolatilitySurface alphaSurface, final VolatilitySurface betaSurface, final VolatilitySurface nuSurface, final VolatilitySurface rhoSurface,
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobian, final Currency currency, final DayCount dayCount) {
    Validate.notNull(alphaSurface, "alpha surface");
    Validate.notNull(betaSurface, "beta surface");
    Validate.notNull(nuSurface, "nu surface");
    Validate.notNull(rhoSurface, "rho surface");
    Validate.notNull(currency, "currency");
    Validate.notNull(dayCount, "day count");
    _alphaSurface = alphaSurface;
    _betaSurface = betaSurface;
    _nuSurface = nuSurface;
    _rhoSurface = rhoSurface;
    _inverseJacobian = inverseJacobian;
    _currency = currency;
    _dayCount = dayCount;
  }

  public VolatilitySurface getAlphaSurface() {
    return _alphaSurface;
  }

  public VolatilitySurface getBetaSurface() {
    return _betaSurface;
  }

  public VolatilitySurface getNuSurface() {
    return _nuSurface;
  }

  public VolatilitySurface getRhoSurface() {
    return _rhoSurface;
  }

  public Map<DoublesPair, DoubleMatrix2D> getInverseJacobians() {
    return _inverseJacobian;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public DayCount getDayCount() {
    return _dayCount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _alphaSurface.hashCode();
    result = prime * result + _betaSurface.hashCode();
    result = prime * result + _nuSurface.hashCode();
    result = prime * result + _rhoSurface.hashCode();
    result = prime * result + _inverseJacobian.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SABRFittedSurfaces)) {
      return false;
    }
    final SABRFittedSurfaces other = (SABRFittedSurfaces) obj;
    if (!ObjectUtils.equals(_alphaSurface, other._alphaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_betaSurface, other._betaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_nuSurface, other._nuSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_rhoSurface, other._rhoSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_inverseJacobian, other._inverseJacobian)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    return true;
  }

}
