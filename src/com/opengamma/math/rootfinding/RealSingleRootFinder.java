/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public abstract class RealSingleRootFinder implements SingleRootFinder<Double, Double> {

  protected void checkInputs(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    if (function == null)
      throw new IllegalArgumentException("Function was null");
    if (x1 == null)
      throw new IllegalArgumentException("First bound was null");
    if (x2 == null)
      throw new IllegalArgumentException("Second bound was null");
  }
}
