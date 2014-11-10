/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Two points are interpolated by an exponential function y = a * exp( b * x ), where a, b are real constants.
 * Note that all of y data should have the same sign.
 */
public class ExponentialInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    Double x1 = boundedValues.getLowerBoundKey();
    Double y1 = boundedValues.getLowerBoundValue();
    if (boundedValues.getLowerBoundIndex() == data.size() - 1) {
      return y1;
    }
    Double x2 = boundedValues.getHigherBoundKey();
    Double y2 = boundedValues.getHigherBoundValue();
    return Math.pow(y2 / y1, (value - x1) / (x2 - x1)) * y1;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    int lowerIndex = data.getLowerBoundIndex(value);
    if (lowerIndex == data.size() - 1) {
      --lowerIndex;
    }
    Double x1 = data.getKeys()[lowerIndex];
    Double y1 = data.getValues()[lowerIndex];
    Double x2 = data.getKeys()[lowerIndex + 1];
    Double y2 = data.getValues()[lowerIndex + 1];
    double xDiffInv = 1.0 / (x2 - x1);
    double y2ovy1 = y2 / y1;
    return Math.pow(y2ovy1, (value - x1) * xDiffInv) * y1 * xDiffInv * Math.log(y2ovy1);
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    int nValues = y.length;
    for (int i = 1; i < nValues; ++i) {
      ArgumentChecker.isTrue(y[i - 1] * y[i] > 0, "All y values should have the same sign");
    }
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    int nValues = y.length;
    for (int i = 1; i < nValues; ++i) {
      ArgumentChecker.isTrue(y[i - 1] * y[i] > 0, "All y values should have the same sign");
    }
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    int size = data.size();
    double[] res = new double[size];
    Arrays.fill(res, 0.0);
    int lowerIndex = data.getLowerBoundIndex(value);
    if (lowerIndex == size - 1) {
      res[size - 1] = 1.0;
      return res;
    }
    Double x1 = data.getKeys()[lowerIndex];
    Double y1 = data.getValues()[lowerIndex];
    Double x2 = data.getKeys()[lowerIndex + 1];
    Double y2 = data.getValues()[lowerIndex + 1];
    double diffInv = 1.0 / (x2 - x1);
    double x1diffInv = (value - x1) * diffInv;
    double x2diffInv = (x2 - value) * diffInv;
    double y1ovy2 = y1 / y2;
    res[lowerIndex] = Math.pow(y1ovy2, -x1diffInv) * x2diffInv;
    res[lowerIndex + 1] = Math.pow(y1ovy2, x2diffInv) * x1diffInv;
    return res;
  }

}
