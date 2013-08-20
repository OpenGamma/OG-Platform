/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * A one-dimensional interpolator. The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub>)</i> and <i>(x<sub>2</sub>, y<sub>2</sub>)</i> is given by:<br>
 * <i>y = y<sub>1</sub> (y<sub>2</sub> / y<sub>1</sub>) ^ ((x - x<sub>1</sub>) /
 * (x<sub>2</sub> - x<sub>1</sub>))</i><br>
 * It is the equivalent of performing a linear interpolation on a data set after
 * taking the logarithm of the y-values.
 * 
 */

public class LogLinearInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle model, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(model, "data bundle");
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final Double x1 = boundedValues.getLowerBoundKey();
    final Double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return y1;
    }
    final Double x2 = boundedValues.getHigherBoundKey();
    final Double y2 = boundedValues.getHigherBoundValue();
    return Math.pow(y2 / y1, (value - x1) / (x2 - x1)) * y1;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle model, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(model, "data bundle");
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final Double x1 = boundedValues.getLowerBoundKey();
    final Double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return 0.;
    }
    final Double x2 = boundedValues.getHigherBoundKey();
    final Double y2 = boundedValues.getHigherBoundValue();
    return Math.pow(y2 / y1, (value - x1) / (x2 - x1)) * y1 * Math.log(y2 / y1) / (x2 - x1);
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }
}
