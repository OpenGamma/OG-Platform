/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.LeastSquare;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.UtilFunctions;
import com.opengamma.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NonlinearLeastSquare {
  private static final double SMALL = Double.MIN_NORMAL;
  private final double[] _x;
  private final double[] _y;
  private final double[] _invSigmaSq;
  private final int _n;
  private final double _eps;
  private final Decomposition<?> _decomposition;
  private final MatrixAlgebra _algebra;

  public NonlinearLeastSquare(double[] x, double[] y, double[] sigma) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    ArgumentChecker.notNull(x, "sigma");
    ArgumentChecker.notEmpty(x, "x");
    ArgumentChecker.notEmpty(y, "y");
    ArgumentChecker.notEmpty(sigma, "sigma");

    int n = x.length;
    if (y.length != n) {
      throw new IllegalArgumentException("y wrong length");
    }
    if (sigma.length != n) {
      throw new IllegalArgumentException("sigma wrong length");
    }
    _n = n;
    _x = x;
    _y = y;
    _invSigmaSq = new double[n];
    for (int i = 0; i < n; i++) {
      if (sigma[i] <= 0.0) {
        throw new IllegalArgumentException("invalide sigma");
      }
      _invSigmaSq[i] = 1 / sigma[i] / sigma[i];
    }
    _decomposition = new LUDecompositionCommons();
    _algebra = new OGMatrixAlgebra();
    _eps = 1e-8;
  }

  public LeastSquareResults solve(final ParameterizedFunction<Double, DoubleMatrix1D, Double> func,
      DoubleMatrix1D startPos) {
    final ScalarFieldFirstOrderDifferentiator diff = new ScalarFieldFirstOrderDifferentiator();

    //if a gradient with respect to the parameters is not supplied, calculate it with finite difference 
    ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> grad = new ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(Double x, DoubleMatrix1D parameters) {
        return diff.derivative(func.asFunctionOfParameters(x)).evaluate(parameters);
      }
    };
    return solve(func, grad, startPos);
  }

  public LeastSquareResults solve(ParameterizedFunction<Double, DoubleMatrix1D, Double> func,
      ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> grad, DoubleMatrix1D startPos) {

    DoubleMatrix1D theta = startPos;

    double lambda = 0.0; //if the model is linear, it will be solved in 1 step
    double newChiSqr, oldChiSqr;
    double[] modelValues = getModelValues(func, theta);
    double[] newModelValues;
    DoubleMatrix1D[] modelGrads = getModelGradients(grad, theta);

    oldChiSqr = getChiSqr(modelValues);
    DoubleMatrix1D beta = getChiSqrGrad(modelValues, modelGrads);
    double g0 = _algebra.getNorm2(beta);

    for (int count = 0; count < 100; count++) {
      DoubleMatrix2D alpha = getModifiedCurvatureMatrix(modelGrads, lambda);
      DecompositionResult decmp = _decomposition.evaluate(alpha);
      DoubleMatrix1D deltaTheta = decmp.solve(beta);
      DoubleMatrix1D newTheta = (DoubleMatrix1D) _algebra.add(theta, deltaTheta);
      newModelValues = getModelValues(func, newTheta);
      newChiSqr = getChiSqr(newModelValues);
      if (newChiSqr < oldChiSqr) {
        lambda /= 10;
        theta = newTheta;
        modelValues = newModelValues;
        modelGrads = getModelGradients(grad, theta);
        beta = getChiSqrGrad(modelValues, modelGrads);

        //check for convergence
        if (_algebra.getNorm2(beta) < _eps * g0) {
          alpha = getModifiedCurvatureMatrix(modelGrads, 0.0);
          decmp = _decomposition.evaluate(alpha);
          DoubleMatrix2D covariance = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(alpha.getNumberOfRows()));
          return new LeastSquareResults(newChiSqr, newTheta, covariance);
        }
        oldChiSqr = newChiSqr;
      } else {
        if (lambda == 0.0) { //this will happen the fist time a full quadratic step fails 
          lambda = 0.01;
        }
        lambda *= 10;
      }
    }
    throw new ConvergenceException("failed to converge");
  }

  private double[] getModelValues(final ParameterizedFunction<Double, DoubleMatrix1D, Double> func, DoubleMatrix1D theta) {
    double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      res[i] = func.evaluate(_x[i], theta);
    }
    return res;
  }

  private DoubleMatrix1D[] getModelGradients(final ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> grad,
      DoubleMatrix1D theta) {
    DoubleMatrix1D[] res = new DoubleMatrix1D[_n];
    for (int i = 0; i < _n; i++) {
      res[i] = grad.evaluate(_x[i], theta);
    }
    return res;
  }

  private double getChiSqr(double[] modelValues) {
    double res = 0.0;
    for (int k = 0; k < _n; k++) {
      res += _invSigmaSq[k] * UtilFunctions.square(_y[k] - modelValues[k]);
    }
    return res;
  }

  private DoubleMatrix1D getChiSqrGrad(double[] modelValues, DoubleMatrix1D[] modelGrads) {
    int size = modelGrads[0].getNumberOfElements();

    double[] beta = new double[size];

    for (int i = 0; i < size; i++) {
      double sum = 0.0;
      for (int k = 0; k < _n; k++) {
        sum += _invSigmaSq[k] * (_y[k] - modelValues[k]) * modelGrads[k].getEntry(i);
      }
      beta[i] = sum;
    }
    return new DoubleMatrix1D(beta);
  }

  private DoubleMatrix2D getModifiedCurvatureMatrix(DoubleMatrix1D[] modelGrads, double lambda) {
    int size = modelGrads[0].getNumberOfElements();
    DoubleMatrix2D res = new DoubleMatrix2D(size, size);
    double[][] alpha = res.getData();

    for (int i = 0; i < size; i++) {
      double sum = 0.0;
      for (int k = 0; k < _n; k++) {
        sum += _invSigmaSq[k] * UtilFunctions.square(modelGrads[k].getEntry(i));
      }
      alpha[i][i] = (1 + lambda) * sum;

      for (int j = i + 1; j < size; j++) {
        sum = 0.0;
        for (int k = 0; k < _n; k++) {
          sum += _invSigmaSq[k] * modelGrads[k].getEntry(i) * modelGrads[k].getEntry(j);
        }
        alpha[i][j] = sum;
      }
    }
    //alpha is symmetric 
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < i; j++) {
        alpha[i][j] = alpha[j][i];
      }
    }
    return res;
  }

}
