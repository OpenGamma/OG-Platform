/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public abstract class Minimizer1D implements Minimizer<Function1D<Double, Double>> {

  protected void checkInputs(final Function1D<Double, Double> f, final double[] initialPoints) {
    Validate.notNull(f);
    Validate.notNull(initialPoints);
    if (initialPoints.length < 2) {
      throw new IllegalArgumentException("Need at least two initial points to attempt to bracket the minimum");
    }
  }

}
