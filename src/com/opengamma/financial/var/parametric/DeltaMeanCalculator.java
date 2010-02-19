/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * @author emcleod
 * 
 */
public class DeltaMeanCalculator extends Function1D<ParametricWithMeanVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaMeanCalculator(final MatrixAlgebra algebra) {
    if (algebra == null)
      throw new IllegalArgumentException("Matrix algebra calculator was null");
    _algebra = algebra;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final ParametricWithMeanVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(Sensitivity.VALUE_DELTA);
    final int s1 = delta.getNumberOfElements();
    if (s1 == 0)
      throw new IllegalArgumentException("Value delta vector contained no data");
    final DoubleMatrix1D mean = data.getMean(Sensitivity.VALUE_DELTA);
    final int s2 = mean.getNumberOfElements();
    if (s2 == 0)
      throw new IllegalArgumentException("Mean vector contained no data");
    if (s1 != s2)
      throw new IllegalArgumentException("Value delta and mean vectors were of different size");
    return _algebra.getInnerProduct(delta, mean);
  }

}
