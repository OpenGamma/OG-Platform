/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.cube;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.Axis;
import com.opengamma.analytics.math.Plane;
import com.opengamma.analytics.math.cube.Cube;
import com.opengamma.analytics.math.cube.CubeShiftFunctionFactory;
import com.opengamma.analytics.math.cube.CubeSliceFunction;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilityCube implements VolatilityModel<Triple<Double, Double, Double>> {
  private final Cube<Double, Double, Double, Double> _cube;
  /** x-axis */
  public static final Axis EXPIRY_AXIS = Axis.X; // TODO Review
  /** y-axis */
  public static final Axis STRIKE_AXIS = Axis.Y;
  /** z-axis */
  public static final Axis MATURITY_AXIS = Axis.Z;

  public VolatilityCube(final Cube<Double, Double, Double, Double> cube) {
    ArgumentChecker.notNull(cube, "cube");
    _cube = cube;
  }

  @Override
  public Double getVolatility(final Triple<Double, Double, Double> xyz) {
    ArgumentChecker.notNull(xyz, "xyz triple");
    return _cube.getValue(xyz);
  }

  /**
   * Return a volatility for the expiry, strike, maturity triple provided.
   * Interpolation/extrapolation behaviour depends on underlying cube
   * @param t time to maturity
   * @param k strike
   * @param m maturity  //TODO review it !
   * @return The Black (implied) volatility  //TODO review it !
   */
  public double getVolatility(final double t, final double k, final double m) {
    final Triple<Double, Double, Double> temp = Triple.of(t, k, m);
    return getVolatility(temp);
  }

  public VolatilitySurface getSlice(final Plane plane, final double here, final Interpolator2D interpolator) {
    final Surface<Double, Double, Double> surface = CubeSliceFunction.cut(_cube, plane, here, interpolator);
    return new VolatilitySurface(surface);
  }

  public Cube<Double, Double, Double, Double> getCube() {
    return _cube;
  }

  public VolatilityCube withParallelShift(final double shift) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, shift, true));
  }

  public VolatilityCube withSingleAdditiveShift(final double x, final double y, final double z, final double shift) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, x, y, z, shift, true));
  }

  public VolatilityCube withMultipleAdditiveShifts(final double[] x, final double[] y, final double[] z, final double[] shifts) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, x, y, z, shifts, true));
  }

  public VolatilityCube withConstantMultiplicativeShift(final double shift) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, shift, false));
  }

  public VolatilityCube withSingleMultiplicativeShift(final double x, final double y, final double z, final double shift) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, x, y, z, shift, false));
  }

  public VolatilityCube withMultipleMultiplicativeShifts(final double[] x, final double[] y, final double[] z, final double[] shifts) {
    return new VolatilityCube(CubeShiftFunctionFactory.getShiftedCube(_cube, x, y, z, shifts, false));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _cube.hashCode();
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
    final VolatilityCube other = (VolatilityCube) obj;
    return ObjectUtils.equals(_cube, other._cube);
  }
}
