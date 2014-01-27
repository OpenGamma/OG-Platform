/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.Plane;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class CubeSliceFunction {

  public Surface<Double, Double, Double> cut(final Cube<Double, Double, Double, Double> cube, final Plane plane, final Double at) {
    Validate.notNull(cube, "cube");
    Validate.notNull(plane, "plane");
    Validate.notNull(at, "at");
    return ConstantDoublesSurface.from(cube.getValue(null, null, null));
  }

  public static Surface<Double, Double, Double> cut(final Cube<Double, Double, Double, Double> cube, final Plane plane, final Double at, final Interpolator2D interpolator) {
    Validate.notNull(cube, "cube");
    Validate.notNull(plane, "plane");
    Validate.notNull(at, "at");

    if (plane == Plane.XY) {
      Double[] xData = cube.getXData();
      Double[] yData = cube.getYData();
      Double[] zData = new Double[xData.length];

      for (int i = 0; i < xData.length; i++) {
        zData[i] = cube.getValue(xData[i], yData[i], at);
      }
      return InterpolatedDoublesSurface.from(xData, yData, zData, interpolator);

    } else if (plane == Plane.YZ) {
      Double[] yData = cube.getYData();
      Double[] zData = cube.getZData();
      Double[] xData = new Double[yData.length];

      for (int i = 0; i < xData.length; i++) {
        xData[i] = cube.getValue(at, yData[i], zData[i]);
      }
      return InterpolatedDoublesSurface.from(xData, yData, zData, interpolator);
    } else if (plane == Plane.ZX) {
      Double[] xData = cube.getXData();
      Double[] zData = cube.getYData();
      Double[] yData = new Double[xData.length];

      for (int i = 0; i < xData.length; i++) {
        yData[i] = cube.getValue(xData[i], at, zData[i]);
      }
      return InterpolatedDoublesSurface.from(xData, yData, zData, interpolator);
    }
    return null; // FIXME Need an example of how to throw exceptions.
  }
}
