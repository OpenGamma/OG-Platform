/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import java.util.List;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 * @param <T> The type of the inputs to the basis functions
 */
public class GeneralizedLeastSquareResults<T> extends LeastSquareResults {

  private final Function1D<T, Double> _function;

  /**
   * @param basisFunctions The basis functions
   * @param chiSq The chi-squared of the fit
   * @param parameters The parameters that were fit
   * @param covariance The covariance matrix of the result
   */
  public GeneralizedLeastSquareResults(final List<Function1D<T, Double>> basisFunctions, final double chiSq, final DoubleMatrix1D parameters, final DoubleMatrix2D covariance) {
    super(chiSq, parameters, covariance, null);

    _function = new BasisFunctionAggregation<T>(basisFunctions, parameters.getData());
  }

  /**
   * Gets the functions field.
   * @return the functions
   */
  public Function1D<T, Double> getFunction() {
    return _function;
  }

}
