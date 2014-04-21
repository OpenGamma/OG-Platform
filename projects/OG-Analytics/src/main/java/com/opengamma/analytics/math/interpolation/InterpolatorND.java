/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public abstract class InterpolatorND implements Interpolator<InterpolatorNDDataBundle, double[]> {

  @Override
  public abstract Double interpolate(InterpolatorNDDataBundle data, double[] x);

  protected void validateInput(final InterpolatorNDDataBundle data, final double[] x) {
    ArgumentChecker.notNull(x, "null position");
    ArgumentChecker.notNull(data, "null databundle");
    final List<Pair<double[], Double>> rawData = data.getData();
    final int dim = x.length;
    ArgumentChecker.isTrue(dim > 0, "0 dimension");
    ArgumentChecker.isTrue(rawData.get(0).getFirst().length == dim, "data and requested point different dimension");
  }

  public abstract InterpolatorNDDataBundle getDataBundle(double[] x, double[] y, double[] z, double[] values);

  public abstract InterpolatorNDDataBundle getDataBundle(List<Pair<double[], Double>> data);

  protected List<Pair<double[], Double>> transformData(final double[] x, final double[] y, final double[] z, final double[] values) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    ArgumentChecker.notNull(z, "z");
    ArgumentChecker.notNull(values, "values");
    final int n = x.length;
    ArgumentChecker.isTrue(y.length == n, "number of ys {} is not equal to number of xs {}", y.length, n);
    ArgumentChecker.isTrue(z.length == n, "number of zs {} is not equal to number of xs {}", z.length, n);
    ArgumentChecker.isTrue(values.length == n, "number of values {} is not equal to number of xs {}", values.length, n);
    final List<Pair<double[], Double>> data = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      data.add(Pairs.of(new double[] {x[i], y[i], z[i]}, values[i]));
    }
    return data;
  }

  /**
   * @param data Interpolator data
   * @param x The co-ordinate at which to calculate the sensitivities.
   * @return The node sensitivities
   */
  public Map<double[], Double> getNodeSensitivitiesForValue(final InterpolatorNDDataBundle data, final double[] x) {
    throw new NotImplementedException("Node sensitivities cannot be calculated by this interpolator");
  }
}
