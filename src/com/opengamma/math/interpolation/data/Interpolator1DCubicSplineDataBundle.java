/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.TridiagonalMatrix;
import com.opengamma.math.linearalgebra.TridiagonalMatrixInvertor;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class Interpolator1DCubicSplineDataBundle implements Interpolator1DDataBundle {
  //TODO use same logic for derivatives and sensitivities
  private final Interpolator1DDataBundle _underlyingData;
  private final double[] _secondDerivatives;
  private double[][] _secondDerivativesSensitivities;
  private final double _leftFirstDev = 0;
  private final double _rightFirstDev = 0;
  private final boolean _leftNatural = true;
  private final boolean _rightNatural = true;

  public Interpolator1DCubicSplineDataBundle(final Interpolator1DDataBundle underlyingData) {
    Validate.notNull(underlyingData);
    _underlyingData = underlyingData;
    _secondDerivatives = getSecondDerivative(underlyingData);
  }

  private double[] getSecondDerivative(final Interpolator1DDataBundle underlyingData) {
    final double[] x = underlyingData.getKeys();
    final double[] y = underlyingData.getValues();
    final int n = x.length;
    final double[] deltaX = new double[n - 1];
    final double[] deltaYOverDeltaX = new double[n - 1];
    final double[] oneOverDeltaX = new double[n - 1];

    for (int i = 0; i < n - 1; i++) {
      deltaX[i] = x[i + 1] - x[i];
      oneOverDeltaX[i] = 1.0 / deltaX[i];
      deltaYOverDeltaX[i] = (y[i + 1] - y[i]) * oneOverDeltaX[i];
    }
    final DoubleMatrix2D inverseTriDiag = getInverseTridiagonalMatrix(deltaX);
    final DoubleMatrix1D rhsVector = getRHSVector(deltaYOverDeltaX);
    return ((DoubleMatrix1D) OG_ALGEBRA.multiply(inverseTriDiag, rhsVector)).getData();
  }

  @Override
  public boolean containsKey(final Double key) {
    return _underlyingData.containsKey(key);
  }

  @Override
  public Double firstKey() {
    return _underlyingData.firstKey();
  }

  @Override
  public Double firstValue() {
    return _underlyingData.firstValue();
  }

  @Override
  public Double get(final Double key) {
    return _underlyingData.get(key);
  }

  @Override
  public InterpolationBoundedValues getBoundedValues(final Double key) {
    return _underlyingData.getBoundedValues(key);
  }

  @Override
  public double[] getKeys() {
    return _underlyingData.getKeys();
  }

  @Override
  public int getLowerBoundIndex(final Double value) {
    return _underlyingData.getLowerBoundIndex(value);
  }

  @Override
  public Double getLowerBoundKey(final Double value) {
    return _underlyingData.getLowerBoundKey(value);
  }

  @Override
  public double[] getValues() {
    return _underlyingData.getValues();
  }

  @Override
  public Double higherKey(final Double key) {
    return _underlyingData.higherKey(key);
  }

  @Override
  public Double higherValue(final Double key) {
    return _underlyingData.higherValue(key);
  }

  @Override
  public Double lastKey() {
    return _underlyingData.lastKey();
  }

  @Override
  public Double lastValue() {
    return _underlyingData.lastValue();
  }

  @Override
  public int size() {
    return _underlyingData.size();
  }

  public double[] getSecondDerivatives() {
    return _secondDerivatives;
  }

  //TODO not ideal that it recomputes the inverse matrix
  public double[][] getSecondDerivativesSensitivities() {
    if (_secondDerivativesSensitivities == null) {
      final double[] x = _underlyingData.getKeys();
      final double[] y = _underlyingData.getValues();
      final int n = x.length;
      final double[] deltaX = new double[n - 1];
      final double[] deltaYOverDeltaX = new double[n - 1];
      final double[] oneOverDeltaX = new double[n - 1];

      for (int i = 0; i < n - 1; i++) {
        deltaX[i] = x[i + 1] - x[i];
        oneOverDeltaX[i] = 1.0 / deltaX[i];
        deltaYOverDeltaX[i] = (y[i + 1] - y[i]) * oneOverDeltaX[i];
      }

      final DoubleMatrix2D inverseTriDiag = getInverseTridiagonalMatrix(deltaX);
      final DoubleMatrix2D rhsMatrix = getRHSMatrix(oneOverDeltaX);
      _secondDerivativesSensitivities = ((DoubleMatrix2D) OG_ALGEBRA.multiply(inverseTriDiag, rhsMatrix)).getData();
    }
    return _secondDerivativesSensitivities;
  }

  private DoubleMatrix2D getRHSMatrix(final double[] oneOverDeltaX) {
    final int n = oneOverDeltaX.length + 1;

    final double[][] res = new double[n][n];
    for (int i = 1; i < n - 1; i++) {
      res[i][i - 1] = oneOverDeltaX[i - 1];
      res[i][i] = -oneOverDeltaX[i] - oneOverDeltaX[i - 1];
      res[i][i + 1] = oneOverDeltaX[i];
    }
    if (!_leftNatural) {
      res[0][0] = oneOverDeltaX[0];
      res[0][1] = -oneOverDeltaX[0];
    }

    if (!_rightNatural) {
      res[n - 1][n - 1] = -oneOverDeltaX[n - 2];
      res[n - 2][n - 2] = oneOverDeltaX[n - 2];
    }
    return new DoubleMatrix2D(res);
  }

  private DoubleMatrix1D getRHSVector(final double[] deltaYOverDeltaX) {
    final int n = deltaYOverDeltaX.length + 1;
    final double[] res = new double[n];

    for (int i = 1; i < n - 1; i++) {
      res[i] = deltaYOverDeltaX[i] - deltaYOverDeltaX[i - 1];
    }
    if (!_leftNatural) {
      res[0] = _leftFirstDev - deltaYOverDeltaX[0];
    }

    if (!_rightNatural) {
      res[n - 1] = _rightFirstDev - deltaYOverDeltaX[n - 2];
    }
    return new DoubleMatrix1D(res);
  }

  private DoubleMatrix2D getInverseTridiagonalMatrix(final double[] deltaX) {
    final TridiagonalMatrixInvertor invertor = new TridiagonalMatrixInvertor();
    final int n = deltaX.length + 1;
    final double[] a = new double[n];
    final double[] b = new double[n - 1];
    final double[] c = new double[n - 1];
    for (int i = 1; i < n - 1; i++) {
      a[i] = (deltaX[i - 1] + deltaX[i]) / 3.0;
      b[i] = deltaX[i] / 6.0;
      c[i - 1] = deltaX[i - 1] / 6.0;
    }
    // Boundary condition
    if (_leftNatural) {
      a[0] = 1.0;
      b[0] = 0.0;
    } else {
      a[0] = -deltaX[0] / 3.0;
      b[0] = deltaX[0] / 6.0;
    }
    if (_rightNatural) {
      a[n - 1] = 1.0;
      c[n - 2] = 0.0;
    } else {
      a[n - 1] = deltaX[n - 2] / 3.0;
      c[n - 2] = deltaX[n - 2] / 6.0;
    }
    final TridiagonalMatrix tridiagonal = new TridiagonalMatrix(a, b, c);
    final DoubleMatrix2D res = invertor.evaluate(tridiagonal);

    return res;

  }
}
