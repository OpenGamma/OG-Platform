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
 * Parent class for root-finders that calculate a root for a vector function (i.e. {@latex.inline $\\mathbf{y} = f(\\mathbf{x})$}, where 
 * {@latex.inline $\\mathbf{x}$} and {@latex.inline $\\mathbf{y}$} are vectors). 
 */
public abstract class VectorRootFinder implements SingleRootFinder<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * {@inheritDoc}
   * Vector root finders only need a single starting point; if more than one is provided, the first is used and any other points ignored.
   */
  @Override
  public DoubleMatrix1D getRoot(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, DoubleMatrix1D... startingPoint) {
    Validate.notNull(startingPoint, "starting point");
    return getRoot(function, startingPoint[0]);
  }

  /**
   * @param function The (vector) function, not null
   * @param x0 The starting point, not null
   * @return The vector root of this function
   */
  public abstract DoubleMatrix1D getRoot(Function1D<DoubleMatrix1D, DoubleMatrix1D> function, DoubleMatrix1D x0);

  /**
   * @param function The function, not null
   * @param x0 The starting point, not null
   */
  protected void checkInputs(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final DoubleMatrix1D x0) {
    Validate.notNull(function);
    Validate.notNull(x0);
  }

}
