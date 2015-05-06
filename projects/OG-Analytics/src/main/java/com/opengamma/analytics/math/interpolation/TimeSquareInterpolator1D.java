/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A one-dimensional interpolator.
 * The interpolation is linear on x y^2. The interpolator is used for interpolation on integrated variance for options.
 * All values of y must be positive. 
 */
public class TimeSquareInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  
  /* Level below which the value is consider to be 0. */
  private static final double EPS = 1.0E-10;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(value, "Value to be interpolated must not be null");
    ArgumentChecker.isTrue(value > 0, "Value should be stricly positive");
    ArgumentChecker.notNull(data, "Data bundle must not be null");
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    if (boundedValues.getLowerBoundIndex() == data.size() - 1) {
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
    ArgumentChecker.notNull(value, "Value to be interpolated must not be null");
    ArgumentChecker.isTrue(value > 0, "Value should be stricly positive");
    ArgumentChecker.notNull(data, "Data bundle must not be null");
    int lowerIndex = data.getLowerBoundIndex(value);
    int index;
    if (lowerIndex == data.size() - 1) {
      index = data.size() - 2;
    } else {
      index = lowerIndex;
    }
    double x1 = data.getKeys()[index];
    double y1 = data.getValues()[index];
    double x2 = data.getKeys()[index + 1];
    double y2 = data.getValues()[index + 1];
    if ((y1 < EPS) || (y2 < EPS)) {
      throw new NotImplementedException("node sensitivity not implemented when one node is 0 value");
    }
    final double w = (x2 - value) / (x2 - x1);
    final double xy21 = x1 * y1 * y1;
    final double xy22 = x2 * y2 * y2;
    final double xy2 = w * xy21 + (1 - w) * xy22;
    return 0.5 * (-Math.sqrt(xy2 / value) + (-xy21 + xy22) / (x2 - x1) / Math.sqrt(xy2 / value)) / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(value, "Value to be interpolated must not be null");
    ArgumentChecker.notNull(data, "Data bundle must not be null");
    final int n = data.size();
    final double[] resultSensitivity = new double[n];
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    final int index = boundedValues.getLowerBoundIndex();
    if (index == n - 1) {
      resultSensitivity[n - 1] = 1.0;
      return resultSensitivity;
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    if ((y1 < EPS) || (y2 < EPS)) {
      throw new NotImplementedException("node sensitivity not implemented when one node is 0 value");
    }
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
      ArgumentChecker.isTrue(y[i] >= 0.0, "All values in y must be positive");
    }
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    ArgumentChecker.notNull(y, "y");
    int nY = y.length;
    for (int i = 0; i < nY; ++i) {
      ArgumentChecker.isTrue(y[i] >= 0.0, "All values in y must be positive");
    }
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
