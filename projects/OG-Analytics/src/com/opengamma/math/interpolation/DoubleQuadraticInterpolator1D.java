/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;

/**
 * 
 */
public class DoubleQuadraticInterpolator1D extends Interpolator1D<Interpolator1DDoubleQuadraticDataBundle> {

  @Override
  public Double interpolate(final Interpolator1DDoubleQuadraticDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (low == 0) {
      final RealPolynomialFunction1D quadratic = data.getQuadratic(0);
      final double x = value - xData[1];
      return quadratic.evaluate(x);
    } else if (high == n) {
      final RealPolynomialFunction1D quadratic = data.getQuadratic(n - 2);
      final double x = value - xData[n - 1];
      return quadratic.evaluate(x);
    } else if (low == n) {
      return yData[n];
    }
    final RealPolynomialFunction1D quadratic1 = data.getQuadratic(low - 1);
    final RealPolynomialFunction1D quadratic2 = data.getQuadratic(high - 1);
    final double w = (xData[high] - value) / (xData[high] - xData[low]);
    final double res = w * quadratic1.evaluate(value - xData[low]) + (1 - w) * quadratic2.evaluate(value - xData[high]);
    return res;
  }

  @Override
  public Interpolator1DDoubleQuadraticDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DDoubleQuadraticDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DDoubleQuadraticDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DDoubleQuadraticDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }

}
