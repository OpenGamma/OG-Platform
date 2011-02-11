/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> Type of the data
 */
public abstract class InterpolatorND<T extends InterpolatorNDDataBundle> implements Interpolator<T, double[]> {

  @Override
  public abstract Double interpolate(T data, double[] x);

  protected void validateInput(T data, double[] x) {
    Validate.notNull(x, "null position");
    Validate.notNull(data, "null databundle");
    List<Pair<double[], Double>> rawData = data.getData();
    int dim = x.length;
    Validate.isTrue(dim > 0, "0 dimension");
    Validate.isTrue(rawData.get(0).getFirst().length == dim, "data and requested point different dimension");
  }

  public abstract T getDataBundle(double[] x, double[] y, double[] z, double[] values);

  public abstract T getDataBundle(List<Pair<double[], Double>> data);

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
}
