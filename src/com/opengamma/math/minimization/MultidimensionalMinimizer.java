/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.FunctionND;

public abstract class MultidimensionalMinimizer implements Minimizer<FunctionND<Double, Double>, Double[]> {

  protected void checkInputs(final FunctionND<Double, Double> f, final Double[][] initialPoints, final int minPoints) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (initialPoints == null)
      throw new IllegalArgumentException("Initial points array was null");
    if (initialPoints.length < minPoints)
      throw new IllegalArgumentException("Need at least one point to start minimization");
    if (initialPoints[0].length != f.getDimension())
      throw new IllegalArgumentException("Dimension of initial point did not match dimension of function");
  }
}
