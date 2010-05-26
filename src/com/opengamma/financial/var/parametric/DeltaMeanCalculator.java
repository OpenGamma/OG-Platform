/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DeltaMeanCalculator extends Function1D<ParametricWithMeanVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final int FIRST_ORDER = 1;

  public DeltaMeanCalculator(final MatrixAlgebra algebra) {
    ArgumentChecker.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final ParametricWithMeanVaRDataBundle data) {
    ArgumentChecker.notNull(data, "data");
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(FIRST_ORDER);
    final int s1 = delta.getNumberOfElements();
    if (s1 == 0)
      throw new IllegalArgumentException("Value delta vector contained no data");
    final DoubleMatrix1D mean = data.getMean(FIRST_ORDER);
    final int s2 = mean.getNumberOfElements();
    if (s2 == 0)
      throw new IllegalArgumentException("Mean vector contained no data");
    if (s1 != s2)
      throw new IllegalArgumentException("Value delta and mean vectors were of different size");
    return _algebra.getInnerProduct(delta, mean);
  }

}
