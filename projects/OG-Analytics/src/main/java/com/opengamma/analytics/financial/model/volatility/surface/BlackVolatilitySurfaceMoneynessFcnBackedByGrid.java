/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.math.surface.Surface;

/**
 *  A surface that contains the Black (implied) volatility  as a function of time to maturity and moneyness, m, defined
 *  as m = k/F(T), where k is the strike and F(T) is the forward for expiry at time T <p>
 *  <p>
 *  The *intention* of this class is to provide access to underlying data grid, vol and axes, so that we may still bump points and project risk across the grid
 */
public class BlackVolatilitySurfaceMoneynessFcnBackedByGrid extends BlackVolatilitySurfaceMoneyness {

  private final SmileSurfaceDataBundle _gridData;
  private final VolatilitySurfaceInterpolator _interpolator;

  public BlackVolatilitySurfaceMoneynessFcnBackedByGrid(final BlackVolatilitySurfaceMoneynessFcnBackedByGrid other) {
    super(other);
    _gridData = other.getGridData();
    _interpolator = other.getInterpolator();
  }

  /**
   * @param surface A implied volatility surface parameterised by time and moneyness m = strike/forward
   * @param forwardCurve the forward curve
   * @param gridData underlying data: expiries, strikes and vols
   * @param interpolator specification of interpolation/extrapolation in expiry and strike dimensions
   */
  public BlackVolatilitySurfaceMoneynessFcnBackedByGrid(final Surface<Double, Double, Double> surface, final ForwardCurve forwardCurve,
      final SmileSurfaceDataBundle gridData, final VolatilitySurfaceInterpolator interpolator) {
    super(surface, forwardCurve);
    Validate.notNull(forwardCurve, "null ForwardCurve");
    Validate.notNull(surface, "null Surface");
    Validate.notNull(gridData, "null SmileSurfaceDataBundle");
    _gridData = gridData;
    _interpolator = interpolator;
  }

  /**
   * Gets the grid data underlying the functional surface. This contains expiries, strikes and vols.
   * @return the gridData
   */
  public final SmileSurfaceDataBundle getGridData() {
    return _gridData;
  }

  /**
   * Gets the interpolator.
   * @return the interpolator
   */
  public final VolatilitySurfaceInterpolator getInterpolator() {
    return _interpolator;
  }

  @Override
  public BlackVolatilitySurfaceMoneynessFcnBackedByGrid withSurface(final Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceMoneynessFcnBackedByGrid(surface, getForwardCurve(), getGridData(), getInterpolator());
  }

  /**
   * @return The Surface as BlackVolatilitySurfaceMoneyness
   */
  public BlackVolatilitySurfaceMoneyness asBVSM() {
    return new BlackVolatilitySurfaceMoneyness(getSurface(), getForwardCurve());
  }

  @Override
  public <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data) {
    return visitor.visitMoneyness(this, data);
  }

  @Override
  public <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor) {
    return visitor.visitMoneyness(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _gridData.hashCode();
    result = prime * result + _interpolator.hashCode();
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
    if (!(obj instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid)) {
      return false;
    }
    final BlackVolatilitySurfaceMoneynessFcnBackedByGrid other = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) obj;
    if (!ObjectUtils.equals(_interpolator, other._interpolator)) {
      return false;
    }
    if (!ObjectUtils.equals(_gridData, other._gridData)) {
      return false;
    }
    return true;
  }

}
