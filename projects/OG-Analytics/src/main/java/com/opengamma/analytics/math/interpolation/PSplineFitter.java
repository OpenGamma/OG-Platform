/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.apache.commons.math.util.MathUtils.binomialCoefficient;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquare;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquareResults;

/**
 * 
 */
public class PSplineFitter {
  private final MatrixAlgebra _algebra = new ColtMatrixAlgebra();
  private final BasisFunctionGenerator _generator = new BasisFunctionGenerator();
  private final GeneralizedLeastSquare _gls = new GeneralizedLeastSquare();

  /**
   * Fits a curve to x-y data
   * @param x The independent variables 
   * @param y The dependent variables 
   * @param sigma The error on the y variables 
   * @param xa The lowest value of x 
   * @param xb The highest value of x 
   * @param nKnots Number of knots (note, the actual number of basis splines and thus fitted weights, equals nKnots + degree)
   * @param degree The degree of the basis function - 0 is piecewise constant, 1 is a sawtooth function (i.e. two straight lines joined in the middle), 2 gives three 
   * quadratic sections joined together, etc. For a large value of degree, the basis function tends to a gaussian 
   * @param lambda The weight given to the penalty function 
   * @param differenceOrder applies the penalty the the nth order difference in the weights, so a differenceOrder of 2 will penalise large 2nd derivatives etc
   * @return The results of the fit
   */
  public GeneralizedLeastSquareResults<Double> solve(final List<Double> x, final List<Double> y, final List<Double> sigma, double xa, double xb, int nKnots, final int degree, final double lambda,
      final int differenceOrder) {
    List<Function1D<Double, Double>> bSplines = _generator.generateSet(xa, xb, nKnots, degree);
    return _gls.solve(x, y, sigma, bSplines, lambda, differenceOrder);
  }

  public GeneralizedLeastSquareResults<double[]> solve(final List<double[]> x, final List<Double> y, final List<Double> sigma, double[] xa, double[] xb, int[] nKnots, final int[] degree,
      final double[] lambda, final int[] differenceOrder) {
    List<Function1D<double[], Double>> bSplines = _generator.generateSet(xa, xb, nKnots, degree);

    int dim = xa.length;
    int[] sizes = new int[dim];
    for (int i = 0; i < dim; i++) {
      sizes[i] = nKnots[i] + degree[i] - 1;
    }
    return _gls.solve(x, y, sigma, bSplines, sizes, lambda, differenceOrder);
  }

  public DoubleMatrix2D getDiffMatrix(final int m, final int k) {
    Validate.isTrue(k < m, "differce order too high");

    final double[][] data = new double[m][m];
    if (m == 0) {
      for (int i = 0; i < m; i++) {
        data[i][i] = 1.0;
      }
      return new DoubleMatrix2D(data);
    }

    final int[] coeff = new int[k + 1];

    int sign = 1;
    for (int i = k; i >= 0; i--) {
      coeff[i] = (int) (sign * binomialCoefficient(k, i));
      sign *= -1;
    }

    for (int i = k; i < m; i++) {
      for (int j = 0; j < k + 1; j++) {
        data[i][j + i - k] = coeff[j];
      }
    }
    final DoubleMatrix2D d = new DoubleMatrix2D(data);

    final DoubleMatrix2D dt = _algebra.getTranspose(d);
    return (DoubleMatrix2D) _algebra.multiply(dt, d);
  }

  public DoubleMatrix2D getDiffMatrix(final int[] size, final int k, final int indices) {
    final int dim = size.length;

    final DoubleMatrix2D d = getDiffMatrix(size[indices], k);

    int preProduct = 1;
    int postProduct = 1;
    for (int j = indices + 1; j < dim; j++) {
      preProduct *= size[j];
    }
    for (int j = 0; j < indices; j++) {
      postProduct *= size[j];
    }
    DoubleMatrix2D temp = d;
    if (preProduct != 1) {
      temp = (DoubleMatrix2D) _algebra.kroneckerProduct(DoubleMatrixUtils.getIdentityMatrix2D(preProduct), temp);
    }
    if (postProduct != 1) {
      temp = (DoubleMatrix2D) _algebra.kroneckerProduct(temp, DoubleMatrixUtils.getIdentityMatrix2D(postProduct));
    }

    return temp;
  }

}
