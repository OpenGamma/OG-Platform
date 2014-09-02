/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * Modification to NonLinearLeastSquare to use a penalty function add to the normal chi^2 term of the form $a^TPa$ where
 * $a$ is the vector of model parameters sort and P is some matrix. The idea is to extend the p-spline concept to
 * non-linear models of the form $\hat{y}_j = H\left(\sum_{i=0}^{M-1} w_i b_i (x_j)\right)$ where $H(\cdot)$ is
 * some non-linear function, $b_i(\cdot)$ are a set of basis functions and $w_i$ are the weights (to be found). As with
 * (linear) p-splines, smoothness of the function is obtained by having a penalty on the nth order difference of the
 * weights. The modified chi-squared is written as
 * $\chi^2 = \sum_{i=0}^{N-1} \left(\frac{y_i-H\left(\sum_{k=0}^{M-1} w_k b_k (x_i)\right)}{\sigma_i} \right)^2 +
 * \sum_{i,j=0}^{M-1}P_{i,j}x_ix_j$
 */
public class NonLinearLeastSquareWithPenalty {
  // private static final Logger LOGGER = LoggerFactory.getLogger(NonLinearLeastSquareWithPenalty.class);
  private static final int MAX_ATTEMPTS = 100000;

  // Review should we use Cholesky as default
  private static final Decomposition<?> DEFAULT_DECOMP = DecompositionFactory.SV_COLT;
  private static final OGMatrixAlgebra MA = new OGMatrixAlgebra();
  private static final double EPS = 1e-8; // Default convergence tolerance on the relative change in chi2

  /**
   * Unconstrained allowed function - always returns true
   */
  public static final Function1D<DoubleMatrix1D, Boolean> UNCONSTRAINED = new Function1D<DoubleMatrix1D, Boolean>() {
    @Override
    public Boolean evaluate(final DoubleMatrix1D x) {
      return true;
    }
  };

  private final double _eps;
  private final Decomposition<?> _decomposition;
  private final MatrixAlgebra _algebra;

  public NonLinearLeastSquareWithPenalty() {
    this(DEFAULT_DECOMP, MA, EPS);
  }

  public NonLinearLeastSquareWithPenalty(final Decomposition<?> decomposition) {
    this(decomposition, MA, EPS);
  }

  public NonLinearLeastSquareWithPenalty(final double eps) {
    this(DEFAULT_DECOMP, MA, eps);
  }

  public NonLinearLeastSquareWithPenalty(final Decomposition<?> decomposition, final double eps) {
    this(decomposition, MA, eps);
  }

  public NonLinearLeastSquareWithPenalty(final Decomposition<?> decomposition, final MatrixAlgebra algebra, final double eps) {
    ArgumentChecker.notNull(decomposition, "decomposition");
    ArgumentChecker.notNull(algebra, "algebra");
    ArgumentChecker.isTrue(eps > 0, "must have positive eps");
    _decomposition = decomposition;
    _algebra = algebra;
    _eps = eps;
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of
   * parameters and return a set of model values,
   * so the measurement points are already known to the function), and analytic parameter sensitivity is not available
   * @param observedValues Set of measurement values
   * @param func The model as a function of its parameters only
   * @param startPos Initial value of the parameters
   * @param penalty Penalty matrix
   * @return value of the fitted parameters
   */
  public LeastSquareWithPenaltyResults solve(final DoubleMatrix1D observedValues, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D startPos, final DoubleMatrix2D penalty) {
    final int n = observedValues.getNumberOfElements();
    final VectorFieldFirstOrderDifferentiator jac = new VectorFieldFirstOrderDifferentiator();
    return solve(observedValues, new DoubleMatrix1D(n, 1.0), func, jac.differentiate(func), startPos, penalty);
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of
   * parameters and return a set of model values,
   * so the measurement points are already known to the function), and analytic parameter sensitivity is not available
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param startPos Initial value of the parameters
   * @param penalty Penalty matrix
   * @return value of the fitted parameters
   */
  public LeastSquareWithPenaltyResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D startPos,
      final DoubleMatrix2D penalty) {
    final VectorFieldFirstOrderDifferentiator jac = new VectorFieldFirstOrderDifferentiator();
    return solve(observedValues, sigma, func, jac.differentiate(func), startPos, penalty);
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of
   * parameters and return a set of model values,
   * so the measurement points are already known to the function), and analytic parameter sensitivity is not available
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param startPos Initial value of the parameters
   * @param penalty Penalty matrix
   * @param allowedValue a function which returned true if the new trial position is allowed by the model. An example
   * would be to enforce positive parameters
   * without resorting to a non-linear parameter transform. In some circumstances this approach will lead to slow
   * convergence.
   * @return value of the fitted parameters
   */
  public LeastSquareWithPenaltyResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D startPos,
      final DoubleMatrix2D penalty, final Function1D<DoubleMatrix1D, Boolean> allowedValue) {
    final VectorFieldFirstOrderDifferentiator jac = new VectorFieldFirstOrderDifferentiator();
    return solve(observedValues, sigma, func, jac.differentiate(func), startPos, penalty, allowedValue);
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of
   * parameters and return a set of model values,
   * so the measurement points are already known to the function), and analytic parameter sensitivity is available
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param jac The model sensitivity to its parameters (i.e. the Jacobian matrix) as a function of its parameters only
   * @param startPos Initial value of the parameters
   * @param penalty Penalty matrix
   * @return the least-square results
   */
  public LeastSquareWithPenaltyResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D startPos, final DoubleMatrix2D penalty) {
    return solve(observedValues, sigma, func, jac, startPos, penalty, UNCONSTRAINED);
  }

  /**
   * Use this when the model is given as a function of its parameters only (i.e. a function that takes a set of
   * parameters and return a set of model values,
   * so the measurement points are already known to the function), and analytic parameter sensitivity is available
   * @param observedValues Set of measurement values
   * @param sigma Set of measurement errors
   * @param func The model as a function of its parameters only
   * @param jac The model sensitivity to its parameters (i.e. the Jacobian matrix) as a function of its parameters only
   * @param startPos Initial value of the parameters
   * @param penalty Penalty matrix (must be positive semi-definite)
   * @param allowedValue a function which returned true if the new trial position is allowed by the model. An example
   * would be to enforce positive parameters
   * without resorting to a non-linear parameter transform. In some circumstances this approach will lead to slow
   * convergence.
   * @return the least-square results
   */
  public LeastSquareWithPenaltyResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D startPos, final DoubleMatrix2D penalty, final Function1D<DoubleMatrix1D, Boolean> allowedValue) {

    Validate.notNull(observedValues, "observedValues");
    Validate.notNull(sigma, " sigma");
    Validate.notNull(func, " func");
    Validate.notNull(jac, " jac");
    Validate.notNull(startPos, "startPos");
    final int nObs = observedValues.getNumberOfElements();
    Validate.isTrue(nObs == sigma.getNumberOfElements(), "observedValues and sigma must be same length");
    ArgumentChecker.isTrue(allowedValue.evaluate(startPos), "The start position {} is not valid for this model. Please choose a valid start position", startPos);

    DoubleMatrix2D alpha;
    DecompositionResult decmp;
    DoubleMatrix1D theta = startPos;

    double lambda = 0.0; // TODO debug if the model is linear, it will be solved in 1 step
    double newChiSqr, oldChiSqr;
    DoubleMatrix1D error = getError(func, observedValues, sigma, theta);

    DoubleMatrix1D newError;
    DoubleMatrix2D jacobian = getJacobian(jac, sigma, theta);

    oldChiSqr = getChiSqr(error);
    double p = getANorm(penalty, theta);
    // if (p < 0.0) {
    // throw new IllegalArgumentException("penalty matrix not positive semi-definite");
    // }
    oldChiSqr += p;

    // If we start at the solution we are done
    if (oldChiSqr == 0.0) {
      return finish(0.0, 0.0, jacobian, theta, sigma);
    }

    DoubleMatrix1D beta = getChiSqrGrad(error, jacobian);
    DoubleMatrix1D temp = (DoubleMatrix1D) _algebra.multiply(penalty, theta);
    beta = (DoubleMatrix1D) _algebra.subtract(beta, temp);

    for (int count = 0; count < MAX_ATTEMPTS; count++) {

      alpha = getModifiedCurvatureMatrix(jacobian, lambda, penalty);
      DoubleMatrix1D deltaTheta;

      try {
        decmp = _decomposition.evaluate(alpha);
        deltaTheta = decmp.solve(beta);
      } catch (final Exception e) {
        throw new MathException(e);
      }

      final DoubleMatrix1D trialTheta = (DoubleMatrix1D) _algebra.add(theta, deltaTheta);

      // if the new value of theta is not in the model domain keep increasing lambda until an acceptable step is found
      if (!allowedValue.evaluate(trialTheta)) {
        lambda = increaseLambda(lambda);
        continue;
      }

      newError = getError(func, observedValues, sigma, trialTheta);
      p = getANorm(penalty, trialTheta);
      newChiSqr = getChiSqr(newError);
      newChiSqr += p;

      // Check for convergence when no improvement in chiSqr occurs
      if (Math.abs(newChiSqr - oldChiSqr) / (1 + oldChiSqr) < _eps) {

        final DoubleMatrix2D alpha0 = lambda == 0.0 ? alpha : getModifiedCurvatureMatrix(jacobian, 0.0, penalty);

        if (lambda > 0.0) {
          decmp = _decomposition.evaluate(alpha0);
        }
        return finish(alpha0, decmp, newChiSqr - p, p, jacobian, trialTheta, sigma);
      }

      if (newChiSqr < oldChiSqr) {
        lambda = decreaseLambda(lambda);
        theta = trialTheta;
        error = newError;
        jacobian = getJacobian(jac, sigma, trialTheta);
        beta = getChiSqrGrad(error, jacobian);
        temp = (DoubleMatrix1D) _algebra.multiply(penalty, theta);
        beta = (DoubleMatrix1D) _algebra.subtract(beta, temp);

        oldChiSqr = newChiSqr;
      } else {
        lambda = increaseLambda(lambda);
      }
    }
    throw new MathException("Could not converge in " + MAX_ATTEMPTS + " attempts");
  }

  private double decreaseLambda(final double lambda) {
    return lambda / 10;
  }

  private double increaseLambda(final double lambda) {
    if (lambda == 0.0) { // this will happen the first time a full quadratic step fails
      return 0.1;
    }
    return lambda * 10;
  }

  /**
   * 
   * the inverse-Jacobian where the i-j entry is the sensitivity of the ith (fitted) parameter (a_i) to the jth data
   * point (y_j).
   * @param sigma Set of measurement errors
   * @param jac The model sensitivity to its parameters (i.e. the Jacobian matrix) as a function of its parameters only
   * @param originalSolution The value of the parameters at a converged solution
   * @return inverse-Jacobian
   */
  public DoubleMatrix2D calInverseJacobian(final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D originalSolution) {
    final DoubleMatrix2D jacobian = getJacobian(jac, sigma, originalSolution);
    final DoubleMatrix2D a = getModifiedCurvatureMatrix(jacobian, 0.0);
    final DoubleMatrix2D bT = getBTranspose(jacobian, sigma);
    final DecompositionResult decRes = _decomposition.evaluate(a);
    return decRes.solve(bT);
  }

  private LeastSquareWithPenaltyResults finish(final double chiSqr, final double penalty, final DoubleMatrix2D jacobian, final DoubleMatrix1D newTheta, final DoubleMatrix1D sigma) {
    final DoubleMatrix2D alpha = getModifiedCurvatureMatrix(jacobian, 0.0);
    final DecompositionResult decmp = _decomposition.evaluate(alpha);
    return finish(alpha, decmp, chiSqr, penalty, jacobian, newTheta, sigma);
  }

  private LeastSquareWithPenaltyResults finish(final DoubleMatrix2D alpha, final DecompositionResult decmp, final double chiSqr, final double penalty, final DoubleMatrix2D jacobian,
      final DoubleMatrix1D newTheta, final DoubleMatrix1D sigma) {
    final DoubleMatrix2D covariance = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(alpha.getNumberOfRows()));
    final DoubleMatrix2D bT = getBTranspose(jacobian, sigma);
    final DoubleMatrix2D inverseJacobian = decmp.solve(bT);
    return new LeastSquareWithPenaltyResults(chiSqr, penalty, newTheta, covariance, inverseJacobian);
  }

  private DoubleMatrix1D getError(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final DoubleMatrix1D theta) {
    final int n = observedValues.getNumberOfElements();
    final DoubleMatrix1D modelValues = func.evaluate(theta);
    Validate.isTrue(n == modelValues.getNumberOfElements(), "Number of data points different between model (" + modelValues.getNumberOfElements() + ") and observed (" + n + ")");
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (observedValues.getEntry(i) - modelValues.getEntry(i)) / sigma.getEntry(i);
    }

    return new DoubleMatrix1D(res);
  }

  private DoubleMatrix2D getBTranspose(final DoubleMatrix2D jacobian, final DoubleMatrix1D sigma) {
    final int n = jacobian.getNumberOfRows();
    final int m = jacobian.getNumberOfColumns();

    DoubleMatrix2D res = new DoubleMatrix2D(m, n);
    double[][] data = res.getData();
    double[][] jacData = jacobian.getData();

    for (int i = 0; i < n; i++) {
      double sigmaInv = 1.0 / sigma.getEntry(i);
      for (int k = 0; k < m; k++) {
        data[k][i] = jacData[i][k] * sigmaInv;
      }
    }
    return res;
  }

  private DoubleMatrix2D getJacobian(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D sigma, final DoubleMatrix1D theta) {
    final DoubleMatrix2D res = jac.evaluate(theta);
    final double[][] data = res.getData();
    final int n = res.getNumberOfRows();
    final int m = res.getNumberOfColumns();
    Validate.isTrue(theta.getNumberOfElements() == m, "Jacobian is wrong size");
    Validate.isTrue(sigma.getNumberOfElements() == n, "Jacobian is wrong size");

    for (int i = 0; i < n; i++) {
      double sigmaInv = 1.0 / sigma.getEntry(i);
      for (int j = 0; j < m; j++) {
        data[i][j] *= sigmaInv;
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

  private DoubleMatrix2D getModifiedCurvatureMatrix(final DoubleMatrix2D jacobian, final double lambda, final DoubleMatrix2D penalty) {
    double onePLambda = 1.0 + lambda;
    final int m = jacobian.getNumberOfColumns();
    DoubleMatrix2D alpha = (DoubleMatrix2D) MA.add(MA.matrixTransposeMultiplyMatrix(jacobian), penalty);
    // scale the diagonal
    double[][] data = alpha.getData();
    for (int i = 0; i < m; i++) {
      data[i][i] *= onePLambda;
    }
    return alpha;
  }

  private DoubleMatrix2D getModifiedCurvatureMatrix(final DoubleMatrix2D jacobian, final double lambda) {

    final int m = jacobian.getNumberOfColumns();
    double onePLambda = 1.0 + lambda;
    DoubleMatrix2D alpha = MA.matrixTransposeMultiplyMatrix(jacobian);
    // scale the diagonal
    double[][] data = alpha.getData();
    for (int i = 0; i < m; i++) {
      data[i][i] *= onePLambda;
    }
    return alpha;
  }

  private double getANorm(final DoubleMatrix2D aM, final DoubleMatrix1D xV) {
    final double[][] a = aM.getData();
    final double[] x = xV.getData();
    final int n = x.length;
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        sum += a[i][j] * x[i] * x[j];
      }
    }
    return sum;
  }

}
