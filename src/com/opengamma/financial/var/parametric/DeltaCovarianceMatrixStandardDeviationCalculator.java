/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
//TODO this needs to be changed to a FirstOrderCMSDC
//similarly for the other classes in this package
public class DeltaCovarianceMatrixStandardDeviationCalculator extends Function1D<ParametricVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final int ORDER = 1;

  public DeltaCovarianceMatrixStandardDeviationCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final ParametricVaRDataBundle data) {
    Validate.notNull(data, "data");
    final Matrix<?> delta = data.getSensitivityData(ORDER);
    final int s1 = delta.getNumberOfElements();
    if (s1 == 0) {
      throw new IllegalArgumentException("Value delta vector contained no data");
    }
    final DoubleMatrix2D covariance = data.getCovarianceMatrix(ORDER);
    return Math.sqrt(_algebra.getInnerProduct(delta, _algebra.multiply(covariance, delta)));
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
    DeltaCovarianceMatrixStandardDeviationCalculator other = (DeltaCovarianceMatrixStandardDeviationCalculator) obj;
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
