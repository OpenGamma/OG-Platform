/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NewtonDefaultUpdateFunction implements NewtonRootFinderMatrixUpdateFunction {

  @Override
  public DoubleMatrix2D getUpdatedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, DoubleMatrix1D x, final DoubleMatrix1D deltaX, final DoubleMatrix1D deltaY,
      final DoubleMatrix2D matrix) {
    Validate.notNull(jacobianFunction);
    Validate.notNull(x);
    return jacobianFunction.evaluate(x);
  }

}
