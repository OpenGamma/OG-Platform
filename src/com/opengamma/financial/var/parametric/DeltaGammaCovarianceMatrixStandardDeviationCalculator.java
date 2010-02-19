/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * @author emcleod
 * 
 */
public class DeltaGammaCovarianceMatrixStandardDeviationCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaGammaCovarianceMatrixStandardDeviationCalculator(final MatrixAlgebra algebra) {
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
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(Sensitivity.VALUE_DELTA);
    final Matrix<?> gamma = data.getSensitivityData(Sensitivity.VALUE_GAMMA);
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(Sensitivity.VALUE_DELTA);
    final double deltaStd = _algebra.getInnerProduct(delta, _algebra.multiply(covariance, delta));
    if (gamma == null || gamma.getNumberOfElements() == 0)
      return Math.sqrt(deltaStd);
    final double gammaStd = 0.5 * _algebra.getTrace(_algebra.getPower(_algebra.multiply(gamma, covariance), 2));
    return Math.sqrt(deltaStd + gammaStd);
  }
}
