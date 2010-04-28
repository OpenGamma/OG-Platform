/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.util.wrapper;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

import com.opengamma.math.function.Function1D;

/**
 * 
 *   Wraps an OpenGamma class in the analogous Commons Math class.
 *
 */
public class CommonsMathWrapper {

  public static UnivariateRealFunction wrap(final Function1D<Double, Double> f) {
    return new UnivariateRealFunction() {

      @Override
      public double value(final double x) throws FunctionEvaluationException {
        return f.evaluate(x);
      }
    };
  }
}
