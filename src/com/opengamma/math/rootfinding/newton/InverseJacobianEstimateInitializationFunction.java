/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
  private final JacobianCalculator _calculator;

  public InverseJacobianEstimateInitializationFunction(final Decomposition<?> decomposition, final JacobianCalculator calculator) {
    Validate.notNull(decomposition);
    Validate.notNull(calculator);
    _decomposition = decomposition;
    _calculator = calculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D getInitializedMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final DoubleMatrix1D x) {
    Validate.notNull(f);
    Validate.notNull(x);
    final DoubleMatrix2D estimate = _calculator.evaluate(x, f);
    final DecompositionResult decompositionResult = _decomposition.evaluate(estimate);
    return decompositionResult.solve(DoubleMatrixUtils.getIdentityMatrix2D(x.getNumberOfElements()));
  }

}
