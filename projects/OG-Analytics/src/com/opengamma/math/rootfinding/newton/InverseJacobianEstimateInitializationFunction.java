/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;

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
