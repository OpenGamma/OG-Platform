/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * For a given sets of knot points (x values) for named curves, and corresponding interpolators, produces a function that takes a set
 * of knot values (y values), which is the concatenation of all curve values as a DoubleMatrix1D, and returns a map between curve names
 * and interpolated curves.
 */
public class InterpolatedCurveBuildingFunction {

  private final LinkedHashMap<String, double[]> _knotPoints;
  private final LinkedHashMap<String, Interpolator1D> _interpolators;
  private final int _nNodes;

  public InterpolatedCurveBuildingFunction(final LinkedHashMap<String, double[]> knotPoints, final LinkedHashMap<String, Interpolator1D> interpolators) {
    ArgumentChecker.notNull(knotPoints, "null knot points");
    ArgumentChecker.notNull(interpolators, "null interpolators");
    int count = 0;
    for (final Map.Entry<String, double[]> entry : knotPoints.entrySet()) {
      final int size = entry.getValue().length;
      ArgumentChecker.isTrue(size > 0, "no knot points for " + entry.getKey());
      count += size;
    }
    _knotPoints = knotPoints;
    _interpolators = interpolators;
    _nNodes = count;
  }

  public LinkedHashMap<String, InterpolatedDoublesCurve> evaluate(final DoubleMatrix1D x) {
    ArgumentChecker.notNull(x, "null data x");
    ArgumentChecker.isTrue(_nNodes == x.getNumberOfElements(), "x wrong length");

    final LinkedHashMap<String, InterpolatedDoublesCurve> res = new LinkedHashMap<>();
    int index = 0;

    for (final String name : _interpolators.keySet()) {
      final Interpolator1D interpolator = _interpolators.get(name);
      final double[] nodes = _knotPoints.get(name);
      final double[] values = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(nodes, values, interpolator);
      res.put(name, curve);
    }

    return res;
  }

  public int getTotalNodes() {
    return _nNodes;
  }

}
