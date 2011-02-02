/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import java.util.List;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.BasisFunctionAggregation;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class GeneralizedLeastSquareResults<T> extends LeastSquareResults {

  private final Function1D<T, Double> _function;

  /**
   * @param chiSq
   * @param parameters
   * @param covariance
   */
  public GeneralizedLeastSquareResults(final List<Function1D<T, Double>> basisFunctions, double chiSq, DoubleMatrix1D parameters, DoubleMatrix2D covariance) {
    super(chiSq, parameters, covariance);

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
