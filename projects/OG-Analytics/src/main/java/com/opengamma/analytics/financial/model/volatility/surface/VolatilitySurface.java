/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.Axis;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.analytics.math.surface.SurfaceSliceFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class VolatilitySurface implements VolatilityModel<DoublesPair> {
  private final Surface<Double, Double, Double> _surface;
  /** x-axis */
  public static final Axis EXPIRY_AXIS = Axis.X; // TODO Review
  /** y-axis */
  public static final Axis STRIKE_AXIS = Axis.Y;

  public VolatilitySurface(final Surface<Double, Double, Double> surface) {
    ArgumentChecker.notNull(surface, "surface");
    _surface = surface;
  }

  @Override
  public Double getVolatility(final DoublesPair xy) {
    ArgumentChecker.notNull(xy, "xy pair");
    return _surface.getZValue(xy);
  }

  /**
   * Return a volatility for the expiry, strike pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility
   */
  public double getVolatility(final double t, final double k) {
    final DoublesPair temp = DoublesPair.of(t, k);
    return getVolatility(temp);
  }


  public VolatilityCurve getSlice(final Axis axis, final double here, final Interpolator1D interpolator) {
    final Curve<Double, Double> curve = SurfaceSliceFunction.cut(_surface, axis, here, interpolator);
    return new VolatilityCurve(curve);
  }

  public Surface<Double, Double, Double> getSurface() {
    return _surface;
  }

  public VolatilitySurface withParallelShift(final double shift) {
    return new VolatilitySurface(getParallelShiftedSurface(shift));
  }

  public VolatilitySurface withSingleAdditiveShift(final double x, final double y, final double shift) {
    return new VolatilitySurface(getSingleAdditiveShiftSurface(x, y, shift));
  }

  public VolatilitySurface withMultipleAdditiveShifts(final double[] x, final double[] y, final double[] shifts) {
    return new VolatilitySurface(getMultipleAdditiveShiftsSurface(x, y, shifts));
  }

  public VolatilitySurface withConstantMultiplicativeShift(final double shift) {
    return new VolatilitySurface(getConstantMultiplicativeShiftSurface(shift));
  }

  public VolatilitySurface withSingleMultiplicativeShift(final double x, final double y, final double shift) {
    return new VolatilitySurface(getSingleMultiplicativeShiftSurface(x, y, shift));
  }

  public VolatilitySurface withMultipleMultiplicativeShifts(final double[] x, final double[] y, final double[] shifts) {
    return new VolatilitySurface(getMultipleMultiplicativeShiftsSurface(x, y, shifts));
  }

  protected Surface<Double, Double, Double> getParallelShiftedSurface(final double shift) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, shift, true);
  }

  protected Surface<Double, Double, Double> getSingleAdditiveShiftSurface(final double x, final double y, final double shift) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, x, y, shift, true);
  }

  protected Surface<Double, Double, Double> getMultipleAdditiveShiftsSurface(final double[] x, final double[] y, final double[] shifts) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, x, y, shifts, true);
  }

  protected Surface<Double, Double, Double> getConstantMultiplicativeShiftSurface(final double shift) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, shift, false);
  }

  protected Surface<Double, Double, Double> getSingleMultiplicativeShiftSurface(final double x, final double y, final double shift) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, x, y, shift, false);
  }

  protected Surface<Double, Double, Double> getMultipleMultiplicativeShiftsSurface(final double[] x, final double[] y, final double[] shifts) {
    return SurfaceShiftFunctionFactory.getShiftedSurface(_surface, x, y, shifts, false);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _surface.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VolatilitySurface other = (VolatilitySurface) obj;
    return ObjectUtils.equals(_surface, other._surface);
  }
}
