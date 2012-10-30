/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class JacobianDirectionFunction implements NewtonRootFinderDirectionFunction {
  private final Decomposition<?> _decomposition;

  public JacobianDirectionFunction(final Decomposition<?> decomposition) {
    Validate.notNull(decomposition);
    _decomposition = decomposition;
  }

  @Override
  public DoubleMatrix1D getDirection(final DoubleMatrix2D estimate, final DoubleMatrix1D y) {
    Validate.notNull(estimate);
    Validate.notNull(y);
    final DecompositionResult result = _decomposition.evaluate(estimate);
    return result.solve(y);
  }

}
