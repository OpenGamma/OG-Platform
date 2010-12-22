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
 */
public class KrigingInterpolatorND extends InterpolatorND {
  private final LUDecompositionQuick _luDecomposition = new LUDecompositionQuick();
  private final double _beta;

  public KrigingInterpolatorND(final double beta) {
    if (beta < 1 || beta >= 2) {
      throw new IllegalArgumentException("Beta was not in acceptable range (1 <= beta < 2");
    }
    _beta = beta;
  }

  // REVIEW this just Gaussian process regression - will probably need to
  // refactor later.

  @Override
  public Double interpolate(final Map<List<Double>, Double> data, final List<Double> value) {
    checkData(data);
    final int dimension = getDimension(data.keySet());
    if (value == null) {
      throw new IllegalArgumentException("Value was null");
    }
    if (value.size() != dimension) {
      throw new IllegalArgumentException("Dimension of value did not match dimension of data");
    }
    final int n = data.size();
    final Function1D<Double, Double> variogram = getVariogram(data, n, dimension);
    final double[] y = getLUSolution(data, n, variogram);
    final double[] v = new double[n + 1];
    final Iterator<Map.Entry<List<Double>, Double>> iter = data.entrySet().iterator();
    Map.Entry<List<Double>, Double> entry;
    for (int i = 0; i < n; i++) {
      entry = iter.next();
      v[i] = variogram.evaluate(getRadius(value, entry.getKey()));
    }
    v[n] = 1;
    // final DoubleMatrix1D vector = DoubleFactory1D.dense.make(v);
    // _luDecomposition.solve(vector);
    double sum = 0;
    for (int i = 0; i <= n; i++) {
      sum += y[i] * v[i];
    }
    return sum;
  }

  private double[] getLUSolution(final Map<List<Double>, Double> data, final int n, final Function1D<Double, Double> variogram) {
    final double[] y = new double[n + 1];
    final double[][] v = new double[n + 1][n + 1];
    final Iterator<Map.Entry<List<Double>, Double>> iter1 = data.entrySet().iterator();

    Iterator<List<Double>> iter2;
    Map.Entry<List<Double>, Double> entry;
    for (int i = 0; i < n; i++) {
      entry = iter1.next();
      y[i] = entry.getValue();
      List<Double> x1 = entry.getKey();

      iter2 = data.keySet().iterator();
      for (int k = 0; k <= i; k++) {
        iter2.next(); // TODO Gets the iterator in the right place. Do something else here
      }

      for (int j = i + 1; j < n; j++) {
        List<Double> x2 = iter2.next();
        double temp = variogram.evaluate(getRadius(x1, x2));
        v[i][j] = temp;
        v[j][i] = v[i][j];
      }
      v[i][n] = 1;
      v[n][i] = 1;
    }
    v[n][n] = 0;
    y[n] = 0;
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(v);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(y);
    _luDecomposition.decompose(matrix);
    _luDecomposition.solve(vector);
    return vector.toArray();
  }

  private Function1D<Double, Double> getVariogram(final Map<List<Double>, Double> data, final int n, final int m) {
    double rb;
    double num = 0, denom = 0;
    final Iterator<Map.Entry<List<Double>, Double>> iter1 = data.entrySet().iterator();
    Iterator<Map.Entry<List<Double>, Double>> iter2;
    Map.Entry<List<Double>, Double> entry1, entry2;
    for (int i = 0; i < n; i++) {
      entry1 = iter1.next();
      iter2 = data.entrySet().iterator();
      for (int j = i + 1; j < n; j++) {
        entry2 = iter2.next();
        rb = 0;
        for (int k = 0; k < m; k++) {
          rb += getRadius(entry1.getKey().get(k), entry2.getKey().get(k));
        }
        rb = Math.pow(rb, 0.5 * _beta);
        num += rb * getRadius(entry1.getValue(), entry2.getValue());
        denom += rb * rb;
      }
    }
    final double alpha = num / 2.0 / denom;
    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        return alpha * Math.pow(x, _beta);
      }

    };
  }

  private double getRadius(final double x1, final double x2) {
    return (x1 - x2) * (x1 - x2);
  }
}
