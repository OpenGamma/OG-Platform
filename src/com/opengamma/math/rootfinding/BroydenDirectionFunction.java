/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class BroydenDirectionFunction implements NewtonRootFinderDirectionFunction {
  private final Decomposition<?> _decomposition;

  public BroydenDirectionFunction(final Decomposition<?> decomposition) {
    Validate.notNull(decomposition);
    _decomposition = decomposition;
  }

  @Override
  public DoubleMatrix1D getDirection(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final DoubleMatrix2D estimate, final DoubleMatrix1D y) {
    Validate.notNull(f);
    Validate.notNull(estimate);
    Validate.notNull(y);
    final DecompositionResult result = _decomposition.evaluate(estimate);
    return result.solve(y);
  }

}
