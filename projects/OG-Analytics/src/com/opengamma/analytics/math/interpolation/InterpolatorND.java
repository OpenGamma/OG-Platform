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
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class InterpolatorND implements Interpolator<InterpolatorNDDataBundle, double[]> {

  @Override
  public abstract Double interpolate(InterpolatorNDDataBundle data, double[] x);

  protected void validateInput(InterpolatorNDDataBundle data, double[] x) {
    Validate.notNull(x, "null position");
    Validate.notNull(data, "null databundle");
    List<Pair<double[], Double>> rawData = data.getData();
    int dim = x.length;
    Validate.isTrue(dim > 0, "0 dimension");
    Validate.isTrue(rawData.get(0).getFirst().length == dim, "data and requested point different dimension");
  }

  public abstract InterpolatorNDDataBundle getDataBundle(double[] x, double[] y, double[] z, double[] values);

  public abstract InterpolatorNDDataBundle getDataBundle(List<Pair<double[], Double>> data);

  protected List<Pair<double[], Double>> transformData(double[] x, double[] y, double[] z, double[] values) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(z, "z");
    Validate.notNull(values, "values");
    int n = x.length;
    Validate.isTrue(y.length == n);
    Validate.isTrue(z.length == n);
    Validate.isTrue(values.length == n);
    List<Pair<double[], Double>> data = new ArrayList<Pair<double[], Double>>(n);
    for (int i = 0; i < n; i++) {
      data.add(Pair.of(new double[] {x[i], y[i], z[i]}, values[i]));
    }
    return data;
  }
  
  public Map<double[], Double> getNodeSensitivitiesForValue(final InterpolatorNDDataBundle data, final double[] x) {
    throw new NotImplementedException("Node sensitivities cannot be calculated by this interpolator");
  }
}
