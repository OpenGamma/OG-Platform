/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;

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
