/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NewtonDefaultUpdateFunction implements NewtonRootFinderMatrixUpdateFunction {
  private final JacobianCalculator _calculator;

  public NewtonDefaultUpdateFunction(JacobianCalculator calculator) {
    Validate.notNull(calculator);
    _calculator = calculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D getUpdatedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, DoubleMatrix1D x, final DoubleMatrix1D deltaX, final DoubleMatrix1D deltaY, final DoubleMatrix2D matrix) {
    Validate.notNull(f);
    Validate.notNull(x);
    return _calculator.evaluate(x, f);
  }

}
