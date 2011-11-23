/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.fittedresults;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class HestonFittedSurfaces {
  private final VolatilitySurface _kappaSurface;
  private final VolatilitySurface _thetaSurface;
  private final VolatilitySurface _vol0Surface;
  private final VolatilitySurface _omegaSurface;
  private final VolatilitySurface _rhoSurface;
  private final Map<DoublesPair, DoubleMatrix2D> _inverseJacobian;
  private final Currency _currency;

  public HestonFittedSurfaces(final VolatilitySurface kappaSurface, final VolatilitySurface thetaSurface, final VolatilitySurface vol0Surface, final VolatilitySurface omegaSurface,
      final VolatilitySurface rhoSurface, final Currency currency) {
    this(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, null, currency);
  }

  public HestonFittedSurfaces(final VolatilitySurface kappaSurface, final VolatilitySurface thetaSurface, final VolatilitySurface vol0Surface, final VolatilitySurface omegaSurface,
      final VolatilitySurface rhoSurface, final Map<DoublesPair, DoubleMatrix2D> inverseJacobian, final Currency currency) {
    Validate.notNull(kappaSurface, "kappa surface");
    Validate.notNull(thetaSurface, "theta surface");
    Validate.notNull(vol0Surface, "vol0 surface");
    Validate.notNull(omegaSurface, "omega surface");
    Validate.notNull(rhoSurface, "rho surface");
    Validate.notNull(currency, "currency");
    _kappaSurface = kappaSurface;
    _thetaSurface = thetaSurface;
    _vol0Surface = vol0Surface;
    _omegaSurface = omegaSurface;
    _rhoSurface = rhoSurface;
    _inverseJacobian = inverseJacobian;
    _currency = currency;
  }

  public VolatilitySurface getKappaSurface() {
    return _kappaSurface;
  }

  public VolatilitySurface getThetaSurface() {
    return _thetaSurface;
  }

  public VolatilitySurface getVol0Surface() {
    return _vol0Surface;
  }

  public VolatilitySurface getOmegaSurface() {
    return _omegaSurface;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _kappaSurface.hashCode();
    result = prime * result + _thetaSurface.hashCode();
    result = prime * result + _vol0Surface.hashCode();
    result = prime * result + _omegaSurface.hashCode();
    result = prime * result + _rhoSurface.hashCode();
    result = prime * result + (_inverseJacobian == null ? 0 : _inverseJacobian.hashCode());
    result = prime * result + _currency.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HestonFittedSurfaces)) {
      return false;
    }
    final HestonFittedSurfaces other = (HestonFittedSurfaces) obj;
    if (!ObjectUtils.equals(_kappaSurface, other._kappaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_thetaSurface, other._thetaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_vol0Surface, other._vol0Surface)) {
      return false;
    }
    if (!ObjectUtils.equals(_omegaSurface, other._omegaSurface)) {
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
    return true;
  }

}
