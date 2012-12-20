/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;

/**
 * 
 */
public class InverseJacobianEstimateInitializationFunction implements NewtonRootFinderMatrixInitializationFunction {
  private final Decomposition<?> _decomposition;

  public InverseJacobianEstimateInitializationFunction(final Decomposition<?> decomposition) {
    Validate.notNull(decomposition);
    _decomposition = decomposition;
  }

  @Override
  public DoubleMatrix2D getInitializedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, final DoubleMatrix1D x) {
    Validate.notNull(jacobianFunction);
    Validate.notNull(x);
    final DoubleMatrix2D estimate = jacobianFunction.evaluate(x);
    final DecompositionResult decompositionResult = _decomposition.evaluate(estimate);
    return decompositionResult.solve(DoubleMatrixUtils.getIdentityMatrix2D(x.getNumberOfElements()));
  }

}
