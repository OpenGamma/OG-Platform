/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public abstract class Minimizer1D implements Minimizer<Function1D<Double, Double>, Double> {

  protected void checkInputs(final Function1D<Double, Double> f, final Double[] initialPoints) {
    if (f == null) {
      throw new IllegalArgumentException("Function was null");
    }
    if (initialPoints == null) {
      throw new IllegalArgumentException("Initial points array was null");
    }
    if (initialPoints.length < 2) {
      throw new IllegalArgumentException("Need at least two initial points to attempt to bracket the minimum");
    }
  }

}
