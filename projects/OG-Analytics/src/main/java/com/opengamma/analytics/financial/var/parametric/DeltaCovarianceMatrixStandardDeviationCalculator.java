/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaCovarianceMatrixStandardDeviationCalculator extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaCovarianceMatrixStandardDeviationCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    Validate.notNull(data, "data");
    final ParametricVaRDataBundle firstOrderData = data.get(1);
    Validate.notNull(firstOrderData, "first order data");
    final Matrix<?> delta = firstOrderData.getSensitivities();
    final int s1 = delta.getNumberOfElements();
    Validate.isTrue(s1 > 0, "Value delta vector contained no data");
    final DoubleMatrix2D covariance = firstOrderData.getCovarianceMatrix();
    return Math.sqrt(_algebra.getInnerProduct(delta, _algebra.multiply(covariance, delta)));
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
    final DeltaCovarianceMatrixStandardDeviationCalculator other = (DeltaCovarianceMatrixStandardDeviationCalculator) obj;
    return ObjectUtils.equals(_algebra, other._algebra);
  }

}
