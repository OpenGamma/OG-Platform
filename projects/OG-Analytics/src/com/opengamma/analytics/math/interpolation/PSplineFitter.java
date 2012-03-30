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

}
