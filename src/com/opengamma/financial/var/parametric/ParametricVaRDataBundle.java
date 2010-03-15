/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.FirstOrder;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * @author emcleod
 * 
 */
public class ParametricVaRDataBundle {
  private final Map<Sensitivity, Matrix<?>> _sensitivities;
  private final Map<Sensitivity, DoubleMatrix2D> _covariances;

  public ParametricVaRDataBundle(final Map<Sensitivity, Matrix<?>> sensitivities, final Map<Sensitivity, DoubleMatrix2D> covariances) {
    _sensitivities = new HashMap<Sensitivity, Matrix<?>>();
    _covariances = new HashMap<Sensitivity, DoubleMatrix2D>();
    testData(sensitivities, covariances);
  }

  public Matrix<?> getSensitivityData(final Sensitivity greek) {
    return _sensitivities.get(greek);
  }

  public DoubleMatrix2D getCovarianceMatrix(final Sensitivity greek) {
    return _covariances.get(greek);
  }

  private void testData(final Map<Sensitivity, Matrix<?>> sensitivities, final Map<Sensitivity, DoubleMatrix2D> covariances) {
    if (sensitivities == null)
      throw new IllegalArgumentException("Sensitivities map was null");
    if (covariances == null)
      throw new IllegalArgumentException("Covariance map was null");
    if (sensitivities.size() < covariances.size())
      throw new IllegalArgumentException("Have more covariance matrices than sensitivity types");
    Matrix<?> m1;
    DoubleMatrix2D m2;
    for (final Sensitivity s : sensitivities.keySet()) {
      m1 = sensitivities.get(s);
      if (m1 == null)
        throw new IllegalArgumentException("Null value for " + s + " in sensitivity data");
      if (s.getOrder() instanceof FirstOrder) {
        if (!(m1 instanceof DoubleMatrix1D)) {
          throw new IllegalArgumentException("First order sensitivities must be a vector, not a matrix (have matrix for " + s + ")");
        } else {
          _sensitivities.put(s, m1);
        }
      } else {
        if (m1 instanceof DoubleMatrix2D) {
          m2 = (DoubleMatrix2D) m1;
          if (m2.getNumberOfColumns() != m2.getNumberOfRows()) {
            throw new IllegalArgumentException("Sensitivity matrix is not square for " + s);
          } else {
            _sensitivities.put(s, m2);
          }
        } else if (m1 instanceof DoubleMatrix1D) {
          _sensitivities.put(s, getDiagonalMatrix((DoubleMatrix1D) m1));
        } else {
          throw new IllegalArgumentException("Can only handle 1D and 2D matrices");
        }
      }
      if (covariances.containsKey(s)) {
        m2 = covariances.get(s);
        if (m2 == null)
          throw new IllegalArgumentException("Null value for " + m2 + " in covariance data");
        if (m2.getNumberOfColumns() != m2.getNumberOfRows())
          throw new IllegalArgumentException("Covariance matrix for " + s + " was not square");
        if (m2.getNumberOfColumns() != (m1 instanceof DoubleMatrix1D ? m1.getNumberOfElements() : ((DoubleMatrix2D) m1).getNumberOfRows()))
          throw new IllegalArgumentException("Covariance matrix and sensitivity matrix sizes do not match for " + s);
        _covariances.put(s, m2);
      }
    }
  }

  private DoubleMatrix2D getDiagonalMatrix(final DoubleMatrix1D secondOrder) {
    final double[] data = secondOrder.getDataAsPrimitiveArray();
    final int n = data.length;
    final double[][] matrix = new double[n][n];
    for (int i = 0; i < n; i++) {
      matrix[i][i] = data[i];
    }
    return new DoubleMatrix2D(matrix);
  }
}
