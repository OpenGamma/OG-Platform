/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Pair;

/**
 * 
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

}
