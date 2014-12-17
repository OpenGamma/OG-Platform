/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A one-dimensional interpolator.
 * The interpolation is linear on x y^2. The interpolator is used for interpolation on integrated variance for options.
 * All values of x, y must be positive. 
 */
public class TimeSquareInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    ArgumentChecker.isTrue(value > 0, "Value should be stricly positive");
    Validate.notNull(data, "Data bundle must not be null");
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    if (data.getLowerBoundIndex(value) == data.size() - 1) {
      return y1;
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    final double w = (x2 - value) / (x2 - x1);
    final double xy21 = x1 * y1 * y1;
    final double xy22 = x2 * y2 * y2;
    final double xy2 = w * xy21 + (1 - w) * xy22;
    return Math.sqrt(xy2 / value);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    ArgumentChecker.isTrue(value > 0, "Value should be stricly positive");
    Validate.notNull(data, "Data bundle must not be null");
    double x1;
    double y1;
    double x2;
    double y2;
    if (data.getLowerBoundIndex(value) == data.size() - 1) {
      int index = data.size() - 2;
      x1 = data.getKeys()[index];
      y1 = data.getValues()[index];
      x2 = data.getKeys()[index + 1];
      y2 = data.getValues()[index + 1];
    } else {
      InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
      x1 = boundedValues.getLowerBoundKey();
      y1 = boundedValues.getLowerBoundValue();
      x2 = boundedValues.getHigherBoundKey();
      y2 = boundedValues.getHigherBoundValue();
    }
    final double w = (x2 - value) / (x2 - x1);
    final double xy21 = x1 * y1 * y1;
    final double xy22 = x2 * y2 * y2;
    final double xy2 = w * xy21 + (1 - w) * xy22;
    return 0.5 * (-Math.sqrt(xy2 / value) + (-xy21 + xy22) / (x2 - x1) / Math.sqrt(xy2 / value)) / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    final int n = data.size();
    final double[] resultSensitivity = new double[n];
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    final int index = data.getLowerBoundIndex(value);
    if (index == n - 1) {
      resultSensitivity[n - 1] = 1.0;
      return resultSensitivity;
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    final double w = (x2 - value) / (x2 - x1);
    final double xy21 = x1 * y1 * y1;
    final double xy22 = x2 * y2 * y2;
    final double xy2 = w * xy21 + (1 - w) * xy22;
    final double resultValue = Math.sqrt(xy2 / value);
    final double resultValueBar = 1.0;
    final double xy2Bar = 0.5 / resultValue / value * resultValueBar;
    final double xy21Bar = w * xy2Bar;
    final double xy22Bar = (1 - w) * xy2Bar;
    final double y2Bar = 2 * x2 * y2 * xy22Bar;
    final double y1Bar = 2 * x1 * y1 * xy21Bar;
    resultSensitivity[index] = y1Bar;
    resultSensitivity[index + 1] = y2Bar;
    return resultSensitivity;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    ArgumentChecker.notNull(y, "y");
    int nY = y.length;
    for (int i = 0; i < nY; ++i) {
      ArgumentChecker.isTrue(y[i] > 0.0, "All values in y must be positive");
    }
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    ArgumentChecker.notNull(y, "y");
    int nY = y.length;
    for (int i = 0; i < nY; ++i) {
      ArgumentChecker.isTrue(y[i] > 0.0, "All values in y must be positive");
    }
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
