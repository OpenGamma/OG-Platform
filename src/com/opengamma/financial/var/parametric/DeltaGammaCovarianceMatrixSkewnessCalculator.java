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
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixSkewnessCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final Sensitivity<Greek> VALUE_DELTA = new ValueGreek(Greek.DELTA);
  private static final Sensitivity<Greek> VALUE_GAMMA = new ValueGreek(Greek.GAMMA);

  public DeltaGammaCovarianceMatrixSkewnessCalculator(final MatrixAlgebra algebra) {
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
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(VALUE_DELTA);
    final Matrix<?> gamma = data.getSensitivityData(VALUE_GAMMA);
    if (gamma == null || gamma.getNumberOfElements() == 0)
      return 0.;
    final DoubleMatrix2D gammaMatrix = (DoubleMatrix2D) gamma;
    final DoubleMatrix2D deltaCovariance = data.getCovarianceMatrix(VALUE_DELTA);
    if (gammaMatrix.getNumberOfColumns() != deltaCovariance.getNumberOfColumns())
      throw new IllegalArgumentException("Gamma matrix and covariance matrix were incompatible sizes");
    final Matrix<?> product = _algebra.multiply(gammaMatrix, deltaCovariance);
    final double numerator = _algebra.getTrace(_algebra.getPower(product, 3)) + 3
        * _algebra.getInnerProduct(delta, _algebra.multiply(_algebra.multiply(deltaCovariance, product), delta));
    final double denominator = Math.pow(0.5 * _algebra.getTrace(_algebra.getPower(product, 2)) + _algebra.getInnerProduct(delta, _algebra.multiply(deltaCovariance, delta)), 1.5);
    return numerator / denominator;
  }
}
