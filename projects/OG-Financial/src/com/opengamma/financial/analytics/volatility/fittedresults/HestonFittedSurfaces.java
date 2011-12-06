/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.fittedresults;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class HestonFittedSurfaces {
  private final InterpolatedDoublesSurface _kappaSurface;
  private final InterpolatedDoublesSurface _thetaSurface;
  private final InterpolatedDoublesSurface _vol0Surface;
  private final InterpolatedDoublesSurface _omegaSurface;
  private final InterpolatedDoublesSurface _rhoSurface;
  private final Map<DoublesPair, DoubleMatrix2D> _inverseJacobian;
  private final Currency _currency;

  public HestonFittedSurfaces(final InterpolatedDoublesSurface kappaSurface, final InterpolatedDoublesSurface thetaSurface, final InterpolatedDoublesSurface vol0Surface,
      final InterpolatedDoublesSurface omegaSurface, final InterpolatedDoublesSurface rhoSurface, final Currency currency) {
    this(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, null, currency);
  }

  public HestonFittedSurfaces(final InterpolatedDoublesSurface kappaSurface, final InterpolatedDoublesSurface thetaSurface, final InterpolatedDoublesSurface vol0Surface,
      final InterpolatedDoublesSurface omegaSurface, final InterpolatedDoublesSurface rhoSurface, final Map<DoublesPair, DoubleMatrix2D> inverseJacobian, final Currency currency) {
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

  public InterpolatedDoublesSurface getKappaSurface() {
    return _kappaSurface;
  }

  public InterpolatedDoublesSurface getThetaSurface() {
    return _thetaSurface;
  }

  public InterpolatedDoublesSurface getVol0Surface() {
    return _vol0Surface;
  }

  public InterpolatedDoublesSurface getOmegaSurface() {
    return _omegaSurface;
  }

  public InterpolatedDoublesSurface getRhoSurface() {
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
