/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.UtilFunctions;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
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
public class NonLinearLeastSquare {

  private final double _eps;
  private final Decomposition<?> _decomposition;
  private final MatrixAlgebra _algebra;

  public NonLinearLeastSquare() {
    _decomposition = new LUDecompositionCommons();
    _algebra = new OGMatrixAlgebra();
    _eps = 1e-8;
  }

  /**
   * Use this when the model is in the ParameterizedFunction form and analytic parameter sensitivity is not available 
   * @param x Set of measurement points 
   * @param y Set of measurement values
   * @param sigma y Set of measurement errors 
   * @param func The model in ParameterizedFunction form (i.e. takes a measurement points and a set of parameters and returns a model value)
   * @param startPos Initial value of the parameters 
   * @return A LeastSquareResults object
   */
  public LeastSquareResults solve(final DoubleMatrix1D x, final DoubleMatrix1D y, final DoubleMatrix1D sigma,
      final ParameterizedFunction<Double, DoubleMatrix1D, Double> func, final DoubleMatrix1D startPos) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(x, "sigma");

    final int n = x.getNumberOfElements();
    if (y.getNumberOfElements() != n) {
      throw new IllegalArgumentException("y wrong length");
    }
    if (sigma.getNumberOfElements() != n) {
      throw new IllegalArgumentException("sigma wrong length");
    }

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func1D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D theta) {
        final int m = x.getNumberOfElements();
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = func.evaluate(x.getEntry(i), theta);
        }
        return new DoubleMatrix1D(res);
      }
    };

    return solve(y, sigma, func1D, startPos);
  }

  /**
   *  Use this when the model is in the ParameterizedFunction form and analytic parameter sensitivity is available 
   * @param x Set of measurement points 
   * @param y Set of measurement values
   * @param sigma Set of measurement errors 
   * @param func The model in ParameterizedFunction form (i.e. takes a measurement points and a set of parameters and returns a model value)
   * @param grad The model parameter sensitivities in  ParameterizedFunction form (i.e. takes a measurement points and a set of parameters and returns a model parameter sensitivities)
   * @param startPos Initial value of the parameters 
   * @return Initial value of the parameters 
   */
  public LeastSquareResults solve(final DoubleMatrix1D x, final DoubleMatrix1D y, final DoubleMatrix1D sigma,
      final ParameterizedFunction<Double, DoubleMatrix1D, Double> func,
      final ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> grad, final DoubleMatrix1D startPos) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    ArgumentChecker.notNull(x, "sigma");

    final int n = x.getNumberOfElements();
    if (y.getNumberOfElements() != n) {
      throw new IllegalArgumentException("y wrong length");
    }
    if (sigma.getNumberOfElements() != n) {
      throw new IllegalArgumentException("sigma wrong length");
    }

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func1D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D theta) {
        final int m = x.getNumberOfElements();
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = func.evaluate(x.getEntry(i), theta);
        }
        return new DoubleMatrix1D(res);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D theta) {
        final int m = x.getNumberOfElements();
        final double[][] res = new double[m][];
        for (int i = 0; i < m; i++) {
          final DoubleMatrix1D temp = grad.evaluate(x.getEntry(i), theta);
          res[i] = temp.getData();
        }
        return new DoubleMatrix2D(res);
      }
    };

    return solve(y, sigma, func1D, jac, startPos);
  }

  /**
   *  Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of parameters and return a set of model values,
   *  so the measurement points are already known to the function), and  analytic parameter sensitivity is not available 
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param startPos  Initial value of the parameters 
   * @return Initial value of the parameters 
   */
  public LeastSquareResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D startPos) {

    final VectorFieldFirstOrderDifferentiator jac = new VectorFieldFirstOrderDifferentiator();
    return solve(observedValues, sigma, func, jac.derivative(func), startPos);
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of parameters and return a set of model values,
   * so the measurement points are already known to the function), and  analytic parameter sensitivity is available 
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param jac The model sensitivity to its parameters (i.e. the Jacobian matrix) as a function of its parameters only
   * @param startPos  Initial value of the parameters 
   * @return Initial value of the parameters 
   */
  public LeastSquareResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac,
      final DoubleMatrix1D startPos) {

    Validate.notNull(observedValues, "observedValues");
    Validate.notNull(sigma, " sigma");
    Validate.notNull(func, " func");
    Validate.notNull(jac, " jac");
    Validate.notNull(startPos, "startPos");
    final int n = observedValues.getNumberOfElements();
    Validate.isTrue(n == sigma.getNumberOfElements(), "observedValues and sigma must be same length");
    Validate.isTrue(n >= startPos.getNumberOfElements(),
        "must have data points greater or equal to number of parameters");

    DoubleMatrix1D theta = startPos;

    double lambda = 0.0; //if the model is linear, it will be solved in 1 step
    double newChiSqr, oldChiSqr;
    DoubleMatrix1D error = getError(func, observedValues, sigma, theta);
    DoubleMatrix1D newError;
    DoubleMatrix2D jacobian = getJacobian(jac, sigma, theta);

    oldChiSqr = getChiSqr(error);
    DoubleMatrix1D beta = getChiSqrGrad(error, jacobian);
    final double g0 = _algebra.getNorm2(beta);

    for (int count = 0; count < 100; count++) {
      DoubleMatrix2D alpha = getModifiedCurvatureMatrix(jacobian, lambda);
      DecompositionResult decmp = _decomposition.evaluate(alpha);
      final DoubleMatrix1D deltaTheta = decmp.solve(beta);
      final DoubleMatrix1D newTheta = (DoubleMatrix1D) _algebra.add(theta, deltaTheta);
      newError = getError(func, observedValues, sigma, newTheta);
      newChiSqr = getChiSqr(newError);
      if (newChiSqr < oldChiSqr) {
        lambda /= 10;
        theta = newTheta;
        error = newError;
        jacobian = getJacobian(jac, sigma, newTheta);
        beta = getChiSqrGrad(error, jacobian);

        //check for convergence
        if (_algebra.getNorm2(beta) < _eps * g0) {
          alpha = getModifiedCurvatureMatrix(jacobian, 0.0);
          decmp = _decomposition.evaluate(alpha);
          final DoubleMatrix2D covariance = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(alpha.getNumberOfRows()));
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
    throw new MathException("failed to converge");
  }

  private DoubleMatrix1D getError(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func,
      final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final DoubleMatrix1D theta) {
    final int n = observedValues.getNumberOfElements();
    final DoubleMatrix1D modelValues = func.evaluate(theta);
    if (modelValues.getNumberOfElements() != n) {
      throw new IllegalArgumentException("Number of data points different between model and observed");
    }
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (observedValues.getEntry(i) - modelValues.getEntry(i)) / sigma.getEntry(i);
    }

    return new DoubleMatrix1D(res);
  }

  private DoubleMatrix2D getJacobian(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D sigma,
      final DoubleMatrix1D theta) {
    final DoubleMatrix2D res = jac.evaluate(theta);
    final double[][] data = res.getData();
    final int m = res.getNumberOfRows();
    final int n = res.getNumberOfColumns();
    if (theta.getNumberOfElements() != n || sigma.getNumberOfElements() != m) {
      throw new IllegalArgumentException("Jacobian is wrong size");
    }

    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        data[i][j] /= sigma.getEntry(i);
      }
    }
    return res;
  }

  private double getChiSqr(final DoubleMatrix1D error) {
    return _algebra.getInnerProduct(error, error);
  }

  private DoubleMatrix1D getChiSqrGrad(final DoubleMatrix1D error, final DoubleMatrix2D jacobian) {
    return (DoubleMatrix1D) _algebra.multiply(error, jacobian);
  }

  private DoubleMatrix2D getModifiedCurvatureMatrix(final DoubleMatrix2D jacobian, final double lambda) {
    final int n = jacobian.getNumberOfRows();
    final int m = jacobian.getNumberOfColumns();

    final DoubleMatrix2D res = new DoubleMatrix2D(m, m);
    final double[][] alpha = res.getData();

    for (int i = 0; i < m; i++) {
      double sum = 0.0;
      for (int k = 0; k < n; k++) {
        sum += UtilFunctions.square(jacobian.getEntry(k, i));
      }
      alpha[i][i] = (1 + lambda) * sum;

      for (int j = i + 1; j < m; j++) {
        sum = 0.0;
        for (int k = 0; k < n; k++) {
          sum += jacobian.getEntry(k, i) * jacobian.getEntry(k, j);
        }
        alpha[i][j] = sum;
      }
    }
    //alpha is symmetric 
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < i; j++) {
        alpha[i][j] = alpha[j][i];
      }
    }
    return res;
  }

}
