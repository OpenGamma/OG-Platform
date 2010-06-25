/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixStandardDeviationCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final int FIRST_ORDER = 1;
  private static final int SECOND_ORDER = 2;

  public DeltaGammaCovarianceMatrixStandardDeviationCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra);
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final ParametricVaRDataBundle data) {
    Validate.notNull(data);
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(FIRST_ORDER);
    final Matrix<?> gamma = data.getSensitivityData(SECOND_ORDER);
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(FIRST_ORDER);
    final double deltaStd = _algebra.getInnerProduct(delta, _algebra.multiply(covariance, delta));
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return Math.sqrt(deltaStd);
    }
    final double gammaStd = 0.5 * _algebra.getTrace(_algebra.getPower(_algebra.multiply(gamma, covariance), 2));
    return Math.sqrt(deltaStd + gammaStd);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_algebra == null) ? 0 : _algebra.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DeltaGammaCovarianceMatrixStandardDeviationCalculator other = (DeltaGammaCovarianceMatrixStandardDeviationCalculator) obj;
    if (_algebra == null) {
      if (other._algebra != null) {
        return false;
      }
    } else if (!_algebra.equals(other._algebra)) {
      return false;
    }
    return true;
  }

}
