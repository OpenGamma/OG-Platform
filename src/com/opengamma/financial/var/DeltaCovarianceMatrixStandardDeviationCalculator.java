/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class DeltaCovarianceMatrixStandardDeviationCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final Algebra _algebra = new Algebra();

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final ParametricVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    final DoubleMatrix1D delta = data.getSensitivityVector(Sensitivity.VALUE_DELTA);
    final int s1 = delta.size();
    if (s1 == 0)
      throw new IllegalArgumentException("Value delta vector contained no data");
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(Sensitivity.VALUE_DELTA);
    final int rows = covariance.rows();
    if (covariance.columns() != rows)
      throw new IllegalArgumentException("Covariance matrix was not square");
    if (rows == 0)
      throw new IllegalArgumentException("Covariance matrix was empty");
    return Math.sqrt(_algebra.mult(delta, _algebra.mult(covariance, delta)));
  }

}
