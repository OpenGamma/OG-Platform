/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.List;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquare;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquareResults;

/**
 * 
 */
public class PSplineFitter {

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
  public GeneralizedLeastSquareResults<Double> solve(final List<Double> x, final List<Double> y, final List<Double> sigma, final double xa, final double xb, final int nKnots, final int degree,
      final double lambda, final int differenceOrder) {
    final List<Function1D<Double, Double>> bSplines = _generator.generateSet(xa, xb, nKnots, degree);
    return _gls.solve(x, y, sigma, bSplines, lambda, differenceOrder);
  }

  public GeneralizedLeastSquareResults<double[]> solve(final List<double[]> x, final List<Double> y, final List<Double> sigma, final double[] xa, final double[] xb, final int[] nKnots,
      final int[] degree, final double[] lambda, final int[] differenceOrder) {
    final List<Function1D<double[], Double>> bSplines = _generator.generateSet(xa, xb, nKnots, degree);

    final int dim = xa.length;
    final int[] sizes = new int[dim];
    for (int i = 0; i < dim; i++) {
      sizes[i] = nKnots[i] + degree[i] - 1;
    }
    return _gls.solve(x, y, sigma, bSplines, sizes, lambda, differenceOrder);
  }

}
