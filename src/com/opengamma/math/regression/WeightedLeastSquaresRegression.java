package com.opengamma.math.regression;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.stat.Probability;

/**
 * 
 * @author emcleod
 * 
 */

public class WeightedLeastSquaresRegression extends LeastSquaresRegression {

  public WeightedLeastSquaresRegression(double[][] x, double[] y, double[] weights, boolean useIntercept) {
    if (x == null) throw new IllegalArgumentException("Independent variable array was null");
    if (y == null) throw new IllegalArgumentException("Dependent variable array was null");
    if (weights == null) throw new IllegalArgumentException("Weight array was null");
    if (x.length == 0 || x[0].length == 0) throw new IllegalArgumentException("No data in independent variable array");
    if (y.length == 0) throw new IllegalArgumentException("No data in dependent variable array");
    if (x.length != y.length) throw new IllegalArgumentException("Dependent and independent variable arrays are not the same length");
    if (weights.length != x.length) throw new IllegalArgumentException("Weight and independent variable arrays are not the same length");
    if (x.length <= x[0].length) throw new IllegalArgumentException("Insufficient data; there are " + y.length + " variables but only " + x[0].length + " data points");
    int length = x[0].length;
    double yMean = y[0];
    for (int i = 1; i < x.length; i++) {
      if (x[i].length != length) {
        throw new IllegalArgumentException("Not all independent variable arrays are the same length");
      }
      yMean += y[i];
    }
    yMean /= y.length;
    double[][] temp;
    if (useIntercept) {
      length = x[0].length + 1;
      temp = new double[x.length][length];
      for (int i = 0; i < x.length; i++) {
        temp[i][0] = 1;
        System.arraycopy(x[i], 0, temp[i], 1, x[i].length);
      }
    } else {
      temp = x;
    }
    performRegression(temp, y, weights, yMean);
  }

  private void performRegression(double[][] x, double[] y, double[] weights, double yMean) {
    Algebra algebra = new Algebra();
    DoubleMatrix2D matrix = DoubleFactory2D.dense.make(x);
    DoubleMatrix1D vector = DoubleFactory1D.dense.make(y);
    DoubleMatrix2D w = DoubleFactory2D.sparse.diagonal(DoubleFactory1D.dense.make(weights));
    DoubleMatrix2D transpose = algebra.transpose(matrix);
    DoubleMatrix1D betas = algebra.mult(algebra.mult(algebra.mult(algebra.inverse(algebra.mult(transpose, algebra.mult(w, matrix))), transpose), w), vector);
    double[] betaArray = betas.toArray();
    setBetas(betaArray);
    double[] yModel = algebra.mult(matrix, betas).toArray();
    double totalSumOfSquares = 0;
    double errorSumOfSquares = 0;
    int n = x.length;
    int k = x[0].length;
    double[][] covarianceBetas = new double[k][k];
    double[] residuals = new double[k];
    double[] stdErrorBetas = new double[k];
    double[] tStats = new double[k];
    double[] pValues = new double[k];
    for (int i = 0; i < k; i++) {
      totalSumOfSquares += weights[i] * (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += weights[i] * residuals[i] * residuals[i];
    }
    setResiduals(residuals);
    setMeanSquareError(errorSumOfSquares / (n - k));
    setRSquared((totalSumOfSquares - errorSumOfSquares) / totalSumOfSquares);
    setAdjustedRSquared(1 - ((n - 1) / (n - k)) * (1 - getRSquared()));
    covarianceBetas = algebra.inverse(algebra.mult(algebra.transpose(matrix), matrix)).toArray();
    for (int i = 0; i < k; i++) {
      stdErrorBetas[i] = Math.sqrt(covarianceBetas[i][i]);
      tStats[i] = Math.sqrt(getMeanSquareError()) * betaArray[i] / stdErrorBetas[i];
      pValues[i] = Probability.studentT(n - k, tStats[i]);
    }
    setStandardErrorOfBeta(stdErrorBetas);
    setTStatistics(tStats);
    setPValues(pValues);
  }
}
