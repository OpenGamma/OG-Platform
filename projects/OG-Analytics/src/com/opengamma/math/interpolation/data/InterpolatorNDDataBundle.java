/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatorNDDataBundle {

  private final List<Pair<double[], Double>> _data;

  public InterpolatorNDDataBundle(final List<Pair<double[], Double>> data) {
    validateData(data);
    _data = data;
  }

  public List<Pair<double[], Double>> getData() {
    return _data;
  }

  private void validateData(final List<Pair<double[], Double>> data) {
    Validate.notEmpty(data, "no data");
    final Iterator<Pair<double[], Double>> iter = data.iterator();
    final int dim = iter.next().getFirst().length;
    Validate.isTrue(dim > 0, "no actual data");
    while (iter.hasNext()) {
      Validate.isTrue(iter.next().getFirst().length == dim, "different dimensions in data");
    }
  }

  //TODO why is this in here?
  public static double getDistance(final double[] x1, final double[] x2) {
    final int dim = x1.length;
    Validate.isTrue(dim == x2.length, "different dimensions");
    double sum = 0;
    double diff;
    for (int i = 0; i < dim; i++) {
      diff = x1[i] - x2[i];
      sum += diff * diff;
    }
    return Math.sqrt(sum);
  }

}
