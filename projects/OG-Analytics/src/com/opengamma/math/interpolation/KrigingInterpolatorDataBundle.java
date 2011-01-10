/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class KrigingInterpolatorDataBundle extends InterpolatorNDDataBundle {

  private final Decomposition<?> _decomp = new LUDecompositionCommons();
  private final Function1D<Double, Double> _variogram;
  private final double[] _weights;

  /**
   * @param data
   */
  public KrigingInterpolatorDataBundle(final List<Pair<double[], Double>> data, double beta) {
    super(data);
    Validate.isTrue(beta >= 1 && beta < 2, "Beta was not in acceptable range (1 <= beta < 2");
    _variogram = calculateVariogram(data, beta);
    _weights = calculateWeights(data, _variogram);
  }

  /**
   * Gets the variogram field.
   * @return the variogram
   */
  public Function1D<Double, Double> getVariogram() {
    return _variogram;
  }

  /**
   * Gets the weights field.
   * @return the weights
   */
  public double[] getWeights() {
    return _weights;
  }

  private Function1D<Double, Double> calculateVariogram(final List<Pair<double[], Double>> data, final double beta) {
    int n = data.size();
    double sum = 0.0;
    double normSum = 0.0;
    double[] x1, x2;
    double y1, y2;
    Pair<double[], Double> dataPoint;
    double rBeta;
    for (int i = 0; i < n; i++) {
      dataPoint = data.get(i);
      x1 = dataPoint.getFirst();
      y1 = dataPoint.getSecond();
      for (int j = i + 1; j < n; j++) {
        dataPoint = data.get(j);
        x2 = dataPoint.getFirst();
        y2 = dataPoint.getSecond();
        rBeta = Math.pow(getDistance(x1, x2), beta);
        sum += (y1 - y2) * (y1 - y2) * rBeta;
        normSum += rBeta * rBeta;
      }
    }

    final double alpha = sum / 2.0 / normSum;

    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        return alpha * Math.pow(x, beta);
      }
    };
  }

  private double[] calculateWeights(final List<Pair<double[], Double>> data, final Function1D<Double, Double> variogram) {
    int n = data.size();
    final double[] y = new double[n + 1];
    final double[][] v = new double[n + 1][n + 1];
    Pair<double[], Double> dataPoint;
    double[] x1, x2;

    for (int i = 0; i < n; i++) {
      dataPoint = data.get(i);
      x1 = dataPoint.getFirst();
      y[i] = dataPoint.getSecond();
      for (int j = i + 1; j < n; j++) {
        dataPoint = data.get(j);
        x2 = dataPoint.getFirst();
        double temp = variogram.evaluate(getDistance(x1, x2));
        v[i][j] = temp;
        v[j][i] = temp;
      }
      v[i][n] = 1;
      v[n][i] = 1;
    }
    v[n][n] = 0;
    y[n] = 0;

    double[] res;
    try {
      res = solve(v, y, _decomp);
    } catch (IllegalArgumentException e) {
      SVDecompositionCommons decomp = new SVDecompositionCommons();
      res = solve(v, y, decomp);
    }

    return res;
  }

  double[] solve(double[][] v, double[] y, Decomposition<?> decomp) {
    DecompositionResult decompRes = decomp.evaluate(new DoubleMatrix2D(v));
    DoubleMatrix1D res = decompRes.solve(new DoubleMatrix1D(y));
    return res.getData();
  }

}
