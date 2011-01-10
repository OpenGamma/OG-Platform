/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * The most common type of multi-dimensional root finding where one has a vector function (i.e maps a DoubleMatrix1D to a DoubleMatrix1D)
 * and wishes to find the vector root
 */
public abstract class VectorRootFinder implements MultiDimensionalRootFinder<DoubleMatrix1D, DoubleMatrix1D> {

  protected void checkInputs(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final DoubleMatrix1D x0) {
    Validate.notNull(function);
    Validate.notNull(x0);
  }

}
