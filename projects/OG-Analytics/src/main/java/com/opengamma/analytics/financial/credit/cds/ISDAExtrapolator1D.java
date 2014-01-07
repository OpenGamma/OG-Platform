/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Curve extrapolation as defined by ISDA
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDAExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {

    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");

    final double[] xValues = data.getKeys();
    final double[] yValues = data.getValues();

    Validate.isTrue(xValues.length == yValues.length, "Invalid data in curve object");
    Validate.isTrue(xValues.length > 1, "At least two data points are required for extrapolation");
    Validate.isTrue(value > xValues[xValues.length - 1], "Value must lie beyond curve data for extrapolation");

    // Avoid divide by zero errors (offset factors out of the final result if it is used)
    final double offset = value == 0.0 ? 1.0 : 0.0;

    final double x1 = xValues[xValues.length - 2];
    final double y1 = yValues[yValues.length - 2];
    final double y1x1 = y1 * (x1 + offset);

    final double x2 = xValues[xValues.length - 1];
    final double y2 = yValues[yValues.length - 1];
    final double y2x2 = y2 * (x2 + offset);

    return (y1x1 + (value - x1) / (x2 - x1) * (y2x2 - y1x1)) / (value + offset);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {

    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");

    final double[] xValues = data.getKeys();
    final double[] yValues = data.getValues();

    Validate.isTrue(xValues.length == yValues.length, "Invalid data in curve object");
    Validate.isTrue(xValues.length > 1, "At least two data points are required for extrapolation");
    Validate.isTrue(value > xValues[xValues.length - 1], "Value must lie beyond curve data for extrapolation");

    // Avoid divide by zero errors (offset factors out of the final result if it is used)
    final double offset = value == 0.0 ? 1.0 : 0.0;

    final double x1 = xValues[xValues.length - 2];
    final double y1 = yValues[yValues.length - 2];
    final double y1x1 = y1 * (x1 + offset);

    final double x2 = xValues[xValues.length - 1];
    final double y2 = yValues[yValues.length - 1];
    final double y2x2 = y2 * (x2 + offset);

    final double valueWithOffset = value + offset;
    return (y2x2 - y1x1) / (x2 - x1) / valueWithOffset - (y1x1 + (value - x1) / (x2 - x1) * (y2x2 - y1x1)) / valueWithOffset / valueWithOffset;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new UnsupportedOperationException("Nodal sensitivities are not supported for the ISDA interpolation method");
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
