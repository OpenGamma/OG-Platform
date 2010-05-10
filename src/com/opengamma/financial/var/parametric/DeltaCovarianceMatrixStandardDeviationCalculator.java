/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaCovarianceMatrixStandardDeviationCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final Sensitivity<Greek> VALUE_DELTA = new ValueGreek(Greek.DELTA);

  public DeltaCovarianceMatrixStandardDeviationCalculator(final MatrixAlgebra algebra) {
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
  public Double evaluate(final ParametricVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    final Matrix<?> delta = data.getSensitivityData(VALUE_DELTA);
    final int s1 = delta.getNumberOfElements();
    if (s1 == 0)
      throw new IllegalArgumentException("Value delta vector contained no data");
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(VALUE_DELTA);
    return Math.sqrt(_algebra.getInnerProduct(delta, _algebra.multiply(covariance, delta)));
  }
}
