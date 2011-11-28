/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * For a given sets of knot points (x values) for named curves, and corresponding interpolators, produces a function that takes a set
 * of knot values (y values), which is the concatenation of all curve values as a DoubleMatrix1D, and returns a map between curve names
 * and interpolated curves.
 */
public class InterpolatedCurveBuildingFunction {

  private final LinkedHashMap<String, double[]> _knotPoints;
  private final LinkedHashMap<String, Interpolator1D> _interpolators;
  private final int _nNodes;

  public InterpolatedCurveBuildingFunction(final LinkedHashMap<String, double[]> knotPoints, LinkedHashMap<String, Interpolator1D> interpolators) {
    Validate.notNull(knotPoints, "null knot points");
    Validate.notNull(interpolators, "null interpolators");
    int count = 0;
    Set<String> names = knotPoints.keySet();
    for (String name : names) {
      int size = knotPoints.get(name).length;
      Validate.isTrue(size > 0, "no knot points for " + name);
      count += size;
    }
    _knotPoints = knotPoints;
    _interpolators = interpolators;
    _nNodes = count;
  }

  public LinkedHashMap<String, InterpolatedDoublesCurve> evaluate(DoubleMatrix1D x) {
    Validate.notNull(x, "null data x");
    Validate.isTrue(_nNodes == x.getNumberOfElements(), "x wrong length");

    LinkedHashMap<String, InterpolatedDoublesCurve> res = new LinkedHashMap<String, InterpolatedDoublesCurve>();
    int index = 0;

    for (final String name : _interpolators.keySet()) {
      final Interpolator1D interpolator = _interpolators.get(name);
      final double[] nodes = _knotPoints.get(name);
      final double[] values = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(nodes, values, interpolator);
      res.put(name, curve);
    }

    return res;
  }

}
