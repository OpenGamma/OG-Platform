/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.LUDecompositionQuick;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class KrigingInterpolatorND extends InterpolatorND {
  private final LUDecompositionQuick _luDecomposition = new LUDecompositionQuick();
  private final Function1D<Double, Double> _variogram;

  public KrigingInterpolatorND(final Function1D<Double, Double> variogram) {
    _variogram = variogram;
  }

  // REVIEW this just Gaussian process regression - will probably need to
  // refactor later.

  @Override
  public InterpolationResult<Double> interpolate(final Map<List<Double>, Double> data, final List<Double> value) {
    checkData(data);
    final int n = data.size();
    final double[] y = getLUSolution(data, n);
    final double[] v = new double[n + 1];
    final Iterator<Map.Entry<List<Double>, Double>> iter = data.entrySet().iterator();
    Map.Entry<List<Double>, Double> entry;
    for (int i = 0; i < n; i++) {
      entry = iter.next();
      v[i] = _variogram.evaluate(getRadius(value, entry.getKey()));
    }
    v[n] = 1;
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(v);
    _luDecomposition.solve(vector);
    double sum = 0, errorSum = 0;
    for (int i = 0; i <= n; i++) {
      sum += y[i] * v[i];
      errorSum += v[i] * vector.get(i);
    }
    return new InterpolationResult<Double>(sum, Math.sqrt(Math.max(0, errorSum)));
  }

  private double[] getLUSolution(final Map<List<Double>, Double> data, final int n) {
    final double[] y = new double[n + 1];
    final double[][] v = new double[n + 1][n + 1];
    final Iterator<Map.Entry<List<Double>, Double>> iter1 = data.entrySet().iterator();
    Iterator<List<Double>> iter2;
    Map.Entry<List<Double>, Double> entry;
    for (int i = 0; i < n; i++) {
      entry = iter1.next();
      y[i] = entry.getValue();
      iter2 = data.keySet().iterator();
      for (int j = i; j < n; j++) {
        v[i][j] = _variogram.evaluate(getRadius(entry.getKey(), iter2.next()));
        v[j][i] = v[i][j];
      }
      v[i][n] = 1;
      v[n][i] = 1;
    }
    v[n][n] = y[n] = 0;
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(v);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(y);
    _luDecomposition.decompose(matrix);
    _luDecomposition.solve(vector);
    return vector.toArray();
  }
}
