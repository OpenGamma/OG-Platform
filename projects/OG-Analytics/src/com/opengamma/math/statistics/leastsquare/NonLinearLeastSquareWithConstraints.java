/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NonLinearLeastSquareWithConstraints extends NonLinearLeastSquare {
  //TODO this is temporary while I see if it works
  private Function1D<DoubleMatrix1D, Boolean> _constaints;

  public LeastSquareResults solve(final DoubleMatrix1D observedValues, final DoubleMatrix1D sigma, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac, final DoubleMatrix1D startPos, final Function1D<DoubleMatrix1D, Boolean> constaints) {
    _constaints = constaints;

    return solve(observedValues, sigma, func, jac, startPos);
  }

  @SuppressWarnings("unused")
  private boolean violatesConstraints(DoubleMatrix1D x) {
    return _constaints.evaluate(x);
  }
}
