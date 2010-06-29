/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class JacobianEstimateInitializationFunction implements NewtonRootFinderMatrixInitializationFunction {
  private final JacobianCalculator _calculator;

  public JacobianEstimateInitializationFunction(final JacobianCalculator calculator) {
    Validate.notNull(calculator);
    _calculator = calculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D getInitializedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final DoubleMatrix1D x) {
    Validate.notNull(f);
    Validate.notNull(x);
    return _calculator.evaluate(x, f);
  }

}
