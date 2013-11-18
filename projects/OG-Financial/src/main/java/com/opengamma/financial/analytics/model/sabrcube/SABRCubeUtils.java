/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.TreeSet;

import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Utility to transform results objects.
 */
public class SABRCubeUtils {

  public static DoubleLabelledMatrix2D toDoubleLabelledMatrix2D(final SurfaceValue surf) {
    final TreeSet<Double> first = new TreeSet<>();
    final TreeSet<Double> second = new TreeSet<>();
    for (final DoublesPair point : surf.getMap().keySet()) {
      first.add(point.getFirst());
      second.add(point.getSecond());
    }
    final Double[] x = first.toArray(new Double[0]);
    final Double[] y = second.toArray(new Double[0]);
    final double[][] v = new double[y.length][x.length];
    for (int loopx = 0; loopx < x.length; loopx++) {
      for (int loopy = 0; loopy < y.length; loopy++) {
        final DoublesPair point = DoublesPair.of(x[loopx].doubleValue(), y[loopy].doubleValue());
        final Double value = surf.getMap().get(point);
        if (value != null) {
          v[loopy][loopx] = value;
        }
      }
    }
    return new DoubleLabelledMatrix2D(x, y, v);
  }

}
