/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PiecewisePolynomialResultsWithSensitivity extends PiecewisePolynomialResult {

  private final DoubleMatrix2D[] _coeffSense;

  /**
   * 
   * @param knots
   * @param coefMatrix
   * @param order
   * @param dim
   * @param coeffSense the sensitivity of the coefficients to the nodes (y-values)
   */
  public PiecewisePolynomialResultsWithSensitivity(DoubleMatrix1D knots, DoubleMatrix2D coefMatrix, int order, int dim, final DoubleMatrix2D[] coeffSense) {
    super(knots, coefMatrix, order, dim);
    if (dim != 1) {
      throw new NotImplementedException();
    }
    ArgumentChecker.noNulls(coeffSense, "null coeffSense"); // coefficient
    _coeffSense = coeffSense;
  }

  public DoubleMatrix2D getCoefficientSensitivity(final int interval) {
    return _coeffSense[interval];
  }

}
