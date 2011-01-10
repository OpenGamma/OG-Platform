/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public abstract class RealSingleRootFinder implements SingleRootFinder<Double, Double> {

  protected void checkInputs(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    Validate.notNull(function);
    Validate.notNull(x1);
    Validate.notNull(x2);
  }
}
