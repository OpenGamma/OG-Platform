/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DistanceCalculator;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class KrigingInterpolatorDataBundle extends InterpolatorNDDataBundle {
  private final Decomposition<?> _decomp = DecompositionFactory.LU_COMMONS;
  private final Function1D<Double, Double> _variogram;
  private final double[] _weights;

  /**
   * @param data The data
   * @param beta The beta
   */
  public KrigingInterpolatorDataBundle(final List<Pair<double[], Double>> data, final double beta) {
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
    final int n = data.size();
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
        rBeta = Math.pow(DistanceCalculator.getDistance(x1, x2), beta);
        sum += (y1 - y2) * (y1 - y2) * rBeta;
        normSum += rBeta * rBeta;
      }
    }

    final double alpha = sum / 2.0 / normSum;

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return alpha * Math.pow(x, beta);
      }
    };
  }

  private double[] calculateWeights(final List<Pair<double[], Double>> data, final Function1D<Double, Double> variogram) {
    final int n = data.size();
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
        final double temp = variogram.evaluate(DistanceCalculator.getDistance(x1, x2));
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
    } catch (final IllegalArgumentException e) {
      final Decomposition<?> decomp = DecompositionFactory.SV_COMMONS;
      res = solve(v, y, decomp);
    }

    return res;
  }

  private double[] solve(final double[][] v, final double[] y, final Decomposition<?> decomp) {
    final DecompositionResult decompRes = decomp.evaluate(new DoubleMatrix2D(v));
    final DoubleMatrix1D res = decompRes.solve(new DoubleMatrix1D(y));
    return res.getData();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _decomp.hashCode();
    result = prime * result + _variogram.hashCode();
    result = prime * result + Arrays.hashCode(_weights);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof KrigingInterpolatorDataBundle)) {
      return false;
    }
    final KrigingInterpolatorDataBundle other = (KrigingInterpolatorDataBundle) obj;
    if (!ObjectUtils.equals(_decomp, other._decomp)) {
      return false;
    }
    if (!ObjectUtils.equals(_variogram, other._variogram)) {
      return false;
    }
    if (!Arrays.equals(_weights, other._weights)) {
      return false;
    }
    return true;
  }

}
