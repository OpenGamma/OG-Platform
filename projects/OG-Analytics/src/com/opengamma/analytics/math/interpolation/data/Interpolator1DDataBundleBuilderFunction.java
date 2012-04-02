/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class Interpolator1DDataBundleBuilderFunction extends Function1D<DoubleMatrix1D, LinkedHashMap<String, Interpolator1DDataBundle>> {

  private final LinkedHashMap<String, double[]> _knotPoints;
  private final LinkedHashMap<String, Interpolator1D> _interpolators;
  private final int _nNodes;

  public Interpolator1DDataBundleBuilderFunction(final LinkedHashMap<String, double[]> knotPoints, final LinkedHashMap<String, Interpolator1D> interpolators) {
    Validate.notNull(knotPoints, "null knot points");
    Validate.notNull(interpolators, "null interpolators");
    int count = 0;
    final Set<String> names = knotPoints.keySet();
    for (final String name : names) {
      final int size = knotPoints.get(name).length;
      Validate.isTrue(size > 0, "no knot points for " + name);
      count += size;
    }
    _knotPoints = knotPoints;
    _interpolators = interpolators;
    _nNodes = count;
  }

  @Override
  public LinkedHashMap<String, Interpolator1DDataBundle> evaluate(final DoubleMatrix1D x) {
    Validate.notNull(x, "null data x");
    Validate.isTrue(_nNodes == x.getNumberOfElements(), "x wrong length");

    final LinkedHashMap<String, Interpolator1DDataBundle> res = new LinkedHashMap<String, Interpolator1DDataBundle>();
    int index = 0;

    for (final String name : _interpolators.keySet()) {
      final Interpolator1D interpolator = _interpolators.get(name);
      final double[] nodes = _knotPoints.get(name);
      final double[] values = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      final Interpolator1DDataBundle db = interpolator.getDataBundleFromSortedArrays(nodes, values);
      res.put(name, db);
    }

    return res;
  }
}
