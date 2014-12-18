/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * The interpolation is linear on y^2. The interpolator is used for interpolation on variance for options.
 * All values of y must be positive. 
 */
public class SquareLinearInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    double x1 = boundedValues.getLowerBoundKey();
    double y1 = boundedValues.getLowerBoundValue();
    if (boundedValues.getLowerBoundIndex() == data.size() - 1) {
      return y1;
    }
    double x2 = boundedValues.getHigherBoundKey();
    double y2 = boundedValues.getHigherBoundValue();
    double w = (x2 - value) / (x2 - x1);
    double y21 = y1 * y1;
    double y22 = y2 * y2;
    double ySq = w * y21 + (1.0 - w) * y22;
    return Math.sqrt(ySq);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
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
    double w = (x2 - value) / (x2 - x1);
    double y21 = y1 * y1;
    double y22 = y2 * y2;
    double ySq = w * y21 + (1.0 - w) * y22;
    return 0.5 * (y22 - y21) / (x2 - x1) / Math.sqrt(ySq);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    int n = data.size();
    double[] resultSensitivity = new double[n];
    InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    double x1 = boundedValues.getLowerBoundKey();
    double y1 = boundedValues.getLowerBoundValue();
    int index = boundedValues.getLowerBoundIndex();
    if (index == n - 1) {
      resultSensitivity[n - 1] = 1.0;
      return resultSensitivity;
    }
    double x2 = boundedValues.getHigherBoundKey();
    double y2 = boundedValues.getHigherBoundValue();
    double w2 = (x2 - value) / (x2 - x1);
    double w1 = 1.0 - w2;
    double y21 = y1 * y1;
    double y22 = y2 * y2;
    double ySq = w2 * y21 + w1 * y22;
    double resultValue = Math.sqrt(ySq);
    resultSensitivity[index] = w2 * y1 / resultValue;
    resultSensitivity[index + 1] = w1 * y2 / resultValue;
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
