/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * @author emcleod
 * 
 */
public class DeltaGammaCovarianceMatrixMeanCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaGammaCovarianceMatrixMeanCalculator(final MatrixAlgebra algebra) {
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
    final Matrix<?> gamma = data.getSensitivityData(Sensitivity.VALUE_GAMMA);
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(Sensitivity.VALUE_DELTA);
    if (gamma == null || gamma.getNumberOfElements() == 0)
      return 0.;
    return 0.5 * _algebra.getTrace(_algebra.multiply(gamma, covariance));
  }
}
