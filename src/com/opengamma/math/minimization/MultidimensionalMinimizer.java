/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.FunctionND;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class MultidimensionalMinimizer implements Minimizer<FunctionND<Double, Double>> {

  protected void checkInputs(final FunctionND<Double, Double> f, final double[] initialPoint) {
    Validate.notNull(f, "function");
    Validate.notNull(initialPoint, "initial point");
    ArgumentChecker.notEmpty(initialPoint, "initial point");
    if (initialPoint.length != f.getDimension()) {
      throw new IllegalArgumentException("Dimension of inital point did not match dimension of function");
    }
  }
}
