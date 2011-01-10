/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixMeanCalculator extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaGammaCovarianceMatrixMeanCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    Validate.notNull(data, "data");
    final ParametricVaRDataBundle firstOrderData = data.get(1);
    Validate.notNull(firstOrderData, "first order data");
    final ParametricVaRDataBundle secondOrderData = data.get(2);
    if (secondOrderData == null) {
      return 0.;
    }
    final Matrix<?> gamma = secondOrderData.getSensitivities();
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return 0.;
    }
    final DoubleMatrix2D covariance = firstOrderData.getCovarianceMatrix();
    return 0.5 * _algebra.getTrace(_algebra.multiply(gamma, covariance));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _algebra.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DeltaGammaCovarianceMatrixMeanCalculator other = (DeltaGammaCovarianceMatrixMeanCalculator) obj;
    return ObjectUtils.equals(_algebra, other._algebra);
  }

}
