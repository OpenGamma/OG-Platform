/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public abstract class SimplexMinimizer implements VectorMinimizer {

  protected void checkInputs(final Function1D<DoubleMatrix1D, Double> f, final DoubleMatrix1D start) {
    Validate.notNull(f, "function");
    Validate.notNull(start, "initial point");
  }
}
