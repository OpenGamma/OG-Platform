/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.LUDecompositionQuick;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorND extends InterpolatorND {
  private final Function1D<Double, Double> _basisFunction;
  private final boolean _useNormalized;
  private final LUDecompositionQuick _luDecomposition = new LUDecompositionQuick();

  public RadialBasisFunctionInterpolatorND(final Function1D<Double, Double> basisFunction, final boolean useNormalized) {
    Validate.notNull(basisFunction, "basis function");
    _basisFunction = basisFunction;
    _useNormalized = useNormalized;
  }

  @Override
  public Double interpolate(final Map<List<Double>, Double> data, final List<Double> value) {
    Validate.notNull(value);
    checkData(data);
    final int dimension = getDimension(data.keySet());
    if (value.size() != dimension) {
      throw new IllegalArgumentException("The value has dimension " + value.size() + "; the dimension of the data was " + dimension);
    }
    double sum = 0;
    double weightedSum = 0;
    double f;
    final double[] w = getWeights(data);
    final Iterator<List<Double>> iter = data.keySet().iterator();
    for (int i = 0; i < data.size(); i++) {
      f = _basisFunction.evaluate(getRadius(value, iter.next()));
      if (Double.isNaN(f) || Double.isInfinite(f)) {
        throw new MathException("Basis function evaluation returned " + f + "; could not calculate interpolated value");
      }
      weightedSum += w[i] * f;
      sum += f;
    }
    return _useNormalized ? weightedSum / sum : weightedSum;
  }

  private double[] getWeights(final Map<List<Double>, Double> data) {
    final int n = data.size();
    double sum, value;
    final double[][] radii = new double[n][n];
    final double[] y = new double[n];
    final Iterator<Map.Entry<List<Double>, Double>> iter1 = data.entrySet().iterator();
    Iterator<List<Double>> iter2;
    List<Double> x1, x2;
    Map.Entry<List<Double>, Double> entry;
    for (int i = 0; i < n; i++) {
      sum = 0;
      entry = iter1.next();
      x1 = entry.getKey();
      iter2 = data.keySet().iterator();
      for (int j = 0; j < n; j++) {
        x2 = iter2.next();
        value = _basisFunction.evaluate(getRadius(x1, x2));
        if (!(Double.isNaN(value) || Double.isInfinite(value))) {
          radii[i][j] = value;
          sum += value;
        }
      }
      y[i] = _useNormalized ? sum * entry.getValue() : entry.getValue();
    }
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(radii);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(y);
    _luDecomposition.decompose(matrix);
    _luDecomposition.solve(vector);
    return vector.toArray();
  }
}
