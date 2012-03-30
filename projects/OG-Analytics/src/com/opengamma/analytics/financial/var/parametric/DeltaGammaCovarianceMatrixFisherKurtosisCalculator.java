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
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixFisherKurtosisCalculator extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {
  private final MatrixAlgebra _algebra;
  private final Function1D<Map<Integer, ParametricVaRDataBundle>, Double> _std;

  public DeltaGammaCovarianceMatrixFisherKurtosisCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
    _std = new DeltaGammaCovarianceMatrixStandardDeviationCalculator(algebra);
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
    final DoubleMatrix1D delta = (DoubleMatrix1D) firstOrderData.getSensitivities();
    final Matrix<?> gamma = secondOrderData.getSensitivities();
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return 0.;
    }
    final DoubleMatrix2D gammaMatrix = (DoubleMatrix2D) gamma;
    final DoubleMatrix2D deltaCovariance = firstOrderData.getCovarianceMatrix();
    if (gammaMatrix.getNumberOfColumns() != deltaCovariance.getNumberOfColumns()) {
      throw new IllegalArgumentException("Gamma matrix and covariance matrix were incompatible sizes");
    }
    final Matrix<?> product = _algebra.multiply(gammaMatrix, deltaCovariance);
    final double std = _std.evaluate(data);
    final double numerator = _algebra.getTrace(_algebra.getPower(product, 4)) + 12
        * _algebra.getInnerProduct(delta, _algebra.multiply(_algebra.multiply(deltaCovariance, _algebra.getPower(product, 2)), delta)) + 3 * std * std;
    final double denominator = Math.pow(0.5 * _algebra.getTrace(_algebra.getPower(product, 2)) + _algebra.getInnerProduct(delta, _algebra.multiply(deltaCovariance, delta)), 2);
    return numerator / denominator - 3;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _algebra.hashCode();
    result = prime * result + _std.hashCode();
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
    final DeltaGammaCovarianceMatrixFisherKurtosisCalculator other = (DeltaGammaCovarianceMatrixFisherKurtosisCalculator) obj;
    return ObjectUtils.equals(_algebra, other._algebra) && ObjectUtils.equals(_std, other._std);
  }
}
