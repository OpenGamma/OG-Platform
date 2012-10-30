/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.fittedresults;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SABRFittedSurfaces {
  private final InterpolatedDoublesSurface _alphaSurface;
  private final InterpolatedDoublesSurface _betaSurface;
  private final InterpolatedDoublesSurface _nuSurface;
  private final InterpolatedDoublesSurface _rhoSurface;
  private final Map<DoublesPair, DoubleMatrix2D> _inverseJacobian;

  public SABRFittedSurfaces(final InterpolatedDoublesSurface alphaSurface, final InterpolatedDoublesSurface betaSurface, final InterpolatedDoublesSurface nuSurface,
      final InterpolatedDoublesSurface rhoSurface, final Map<DoublesPair, DoubleMatrix2D> inverseJacobian) {
    ArgumentChecker.notNull(alphaSurface, "alpha surface");
    ArgumentChecker.notNull(betaSurface, "beta surface");
    ArgumentChecker.notNull(nuSurface, "nu surface");
    ArgumentChecker.notNull(rhoSurface, "rho surface");
    _alphaSurface = alphaSurface;
    _betaSurface = betaSurface;
    _nuSurface = nuSurface;
    _rhoSurface = rhoSurface;
    _inverseJacobian = inverseJacobian;
  }

  public InterpolatedDoublesSurface getAlphaSurface() {
    return _alphaSurface;
  }

  public InterpolatedDoublesSurface getBetaSurface() {
    return _betaSurface;
  }

  public InterpolatedDoublesSurface getNuSurface() {
    return _nuSurface;
  }

  public InterpolatedDoublesSurface getRhoSurface() {
    return _rhoSurface;
  }

  public Map<DoublesPair, DoubleMatrix2D> getInverseJacobians() {
    return _inverseJacobian;
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
    return true;
  }

  @Override
  public String toString() {
    return "SABR Fitted Surfaces";
  }
}
