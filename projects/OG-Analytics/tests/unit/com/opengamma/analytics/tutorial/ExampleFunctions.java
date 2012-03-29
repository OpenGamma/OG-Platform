/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial;

import static com.opengamma.analytics.math.FunctionUtils.square;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Menu of functions used in the tutorials
 */
public class ExampleFunctions {

  /**
   * Computes a 1D Matrix by computing the square of each element of another
   */
  public static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SQUARES = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final int n = x.getNumberOfElements();
      final double[] y = new double[n];
      for (int i = 0; i < n; i++) {
        y[i] = square(x.getEntry(i));
      }

      return new DoubleMatrix1D(y);
    }
  };

}
