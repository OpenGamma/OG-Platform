/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricVaRDataBundle {
  private final Map<Integer, Matrix<?>> _sensitivities;
  private final Map<Integer, DoubleMatrix2D> _covariances;

  //TODO rewrite in the same way as SensitivityPnLCalculator
  public ParametricVaRDataBundle(final Map<Integer, Matrix<?>> sensitivities, final Map<Integer, DoubleMatrix2D> covariances) {
    Validate.notNull(sensitivities, "sensitivites");
    Validate.notNull(covariances, "covariances");
    _sensitivities = new HashMap<Integer, Matrix<?>>();
    _covariances = new HashMap<Integer, DoubleMatrix2D>();
    testData(sensitivities, covariances);
  }

  public Matrix<?> getSensitivityData(final int order) {
    return _sensitivities.get(order);
  }

  public DoubleMatrix2D getCovarianceMatrix(final int order) {
    return _covariances.get(order);
  }

  private void testData(final Map<Integer, Matrix<?>> sensitivities, final Map<Integer, DoubleMatrix2D> covariances) {
    if (sensitivities.size() < covariances.size()) {
      throw new IllegalArgumentException("Have more covariance matrices than sensitivity types");
    }
    Matrix<?> m1;
    DoubleMatrix2D m2;
    for (final Integer order : sensitivities.keySet()) {
      m1 = sensitivities.get(order);
      if (m1 == null) {
        throw new IllegalArgumentException("Null value for order " + order + " in sensitivity data");
      }
      if (order == 1) {
        if (!(m1 instanceof DoubleMatrix1D)) {
          throw new IllegalArgumentException("First order sensitivities must be a vector, not a matrix (have matrix for order " + order + ")");
        }
        _sensitivities.put(order, m1);
      } else {
        if (m1 instanceof DoubleMatrix2D) {
          m2 = (DoubleMatrix2D) m1;
          if (m2.getNumberOfColumns() != m2.getNumberOfRows()) {
            throw new IllegalArgumentException("Sensitivity matrix is not square for order " + order);
          }
          _sensitivities.put(order, m2);
        } else if (m1 instanceof DoubleMatrix1D) {
          _sensitivities.put(order, getDiagonalMatrix((DoubleMatrix1D) m1));
        } else {
          throw new IllegalArgumentException("Can only handle 1D and 2D matrices");
        }
      }
      if (covariances.containsKey(order)) {
        m2 = covariances.get(order);
        if (m2 == null) {
          throw new IllegalArgumentException("Null value for " + m2 + " in covariance data");
        }
        if (m2.getNumberOfColumns() != m2.getNumberOfRows()) {
          throw new IllegalArgumentException("Covariance matrix for order " + order + " was not square");
        }
        if (m2.getNumberOfColumns() != (m1 instanceof DoubleMatrix1D ? m1.getNumberOfElements() : ((DoubleMatrix2D) m1).getNumberOfRows())) {
          throw new IllegalArgumentException("Covariance matrix and sensitivity matrix sizes do not match for order " + order);
        }
        _covariances.put(order, m2);
      }
    }
  }

  private DoubleMatrix2D getDiagonalMatrix(final DoubleMatrix1D secondOrder) {
    final double[] data = secondOrder.getData();
    final int n = data.length;
    final double[][] matrix = new double[n][n];
    for (int i = 0; i < n; i++) {
      matrix[i][i] = data[i];
    }
    return new DoubleMatrix2D(matrix);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_covariances == null) ? 0 : _covariances.hashCode());
    result = prime * result + ((_sensitivities == null) ? 0 : _sensitivities.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ParametricVaRDataBundle other = (ParametricVaRDataBundle) obj;
    if (_covariances == null) {
      if (other._covariances != null) {
        return false;
      }
    } else if (!_covariances.equals(other._covariances)) {
      return false;
    }
    if (_sensitivities == null) {
      if (other._sensitivities != null) {
        return false;
      }
    } else if (!_sensitivities.equals(other._sensitivities)) {
      return false;
    }
    return true;
  }

}
