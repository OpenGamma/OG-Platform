/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * A one-dimensional linear interpolator. The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub>)</i> and <i>(x<sub>2</sub>, y<sub>2</sub>)</i> is given by:<br>
 * <i>y = y<sub>1</sub> + (x - x<sub>1</sub>) * (y<sub>2</sub> - y<sub>1</sub>)
 * / (x<sub>2</sub> - x<sub>1</sub>)</i>
 */
public class LinearInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Data bundle must not be null");
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return y1;
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    return y1 + (value - x1) / (x2 - x1) * (y2 - y1);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Data bundle must not be null");
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      if (value > model.lastKey()) {
        throw new MathException("Value of " + value + " after last key. Use exstrapolator");
      }
      final double[] x = model.getKeys();
      final double[] y = model.getValues();
      final int n = x.length;
      return n == 1 ? 0.0 : (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]);
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    return (y2 - y1) / (x2 - x1);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    final int n = data.size();
    final double[] result = new double[n];
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null) {
      result[n - 1] = 1.0;
      return result;
    }
    final int index = data.getLowerBoundIndex(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double x2 = boundedValues.getHigherBoundKey();
    final double dx = x2 - x1;
    final double a = (x2 - value) / dx;
    final double b = 1 - a;
    result[index] = a;
    result[index + 1] = b;
    return result;
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
