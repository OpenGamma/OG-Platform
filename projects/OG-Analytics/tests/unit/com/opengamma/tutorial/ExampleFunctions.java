/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.tutorial;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import static com.opengamma.math.FunctionUtils.square;

/**
 * Menu of functions used in the tutorials
 */
public class ExampleFunctions {
  /**
   * Computes a 1D Matrix by computing the square of each element of another
   */
  public static Function1D<DoubleMatrix1D, DoubleMatrix1D> Squares = new Function1D<DoubleMatrix1D, DoubleMatrix1D>()
  {
    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x)
    {
      int n = x.getNumberOfElements();
      double[] y = new double[n];
      for (int i = 0; i < n; i++)
        y[i] = square(x.getEntry(i));
      
      return new DoubleMatrix1D(y);
    }
  };
}
