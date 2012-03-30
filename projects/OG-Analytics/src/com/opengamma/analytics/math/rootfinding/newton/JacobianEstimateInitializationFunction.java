/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class JacobianEstimateInitializationFunction implements NewtonRootFinderMatrixInitializationFunction {

  @Override
  public DoubleMatrix2D getInitializedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, final DoubleMatrix1D x) {
    ArgumentChecker.notNull(jacobianFunction, "Jacobian Function");
    ArgumentChecker.notNull(x, "x");
    return jacobianFunction.evaluate(x);
  }

}
