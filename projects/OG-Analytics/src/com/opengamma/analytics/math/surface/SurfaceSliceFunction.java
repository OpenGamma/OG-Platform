/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.Axis;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 * 
 */
public class SurfaceSliceFunction {

  public ConstantDoublesCurve cut(final ConstantDoublesSurface surface, final Axis axis, final Double at) {
    Validate.notNull(surface, "surface");
    Validate.notNull(axis, "axis");
    Validate.notNull(at, "at");
    return ConstantDoublesCurve.from(surface.getZValue(null, null));
  }

  public static Curve<Double, Double> cut(final Surface<Double, Double, Double> surface, final Axis axis, final Double at, final Interpolator1D interpolator) {
    Validate.notNull(surface, "surface");
    Validate.notNull(axis, "axis");
    Validate.notNull(at, "at");

    if (axis == Axis.X) {
      Double[] yData = surface.getYData();
      int nPoints = yData.length;
      Double[] zData = new Double[nPoints];
      for (int i = 0; i < nPoints; i++) {
        zData[i] = surface.getZValue(at, yData[i]);
      }
      return InterpolatedDoublesCurve.from(yData, zData, interpolator);

    } else if (axis == Axis.Y) {
      Double[] xData = surface.getXData();
      int nPoints = xData.length;
      Double[] zData = new Double[nPoints];
      for (int i = 0; i < nPoints; i++) {
        zData[i] = surface.getZValue(at, xData[i]);
      }
      return InterpolatedDoublesCurve.from(xData, zData, interpolator);
    }
    return null; // FIXME Need an example of how to throw exceptions.
  }
}
