/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class InverseJacobianDirectionFunction implements NewtonRootFinderDirectionFunction {
  private final MatrixAlgebra _algebra;

  public InverseJacobianDirectionFunction(final MatrixAlgebra algebra) {
    Validate.notNull(algebra);
    _algebra = algebra;
  }

  @Override
  public DoubleMatrix1D getDirection(final DoubleMatrix2D estimate, final DoubleMatrix1D y) {
    Validate.notNull(estimate);
    Validate.notNull(y);
    return (DoubleMatrix1D) _algebra.multiply(estimate, y);
  }

}
