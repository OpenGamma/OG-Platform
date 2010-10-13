/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaMeanCalculator extends Function1D<ParametricWithMeanVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;

  public DeltaMeanCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final ParametricWithMeanVaRDataBundle data) {
    Validate.notNull(data, "data");
    Validate.isTrue(data.getOrder() == 1, "Must have first order sensitivities");
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivities();
    final int s1 = delta.getNumberOfElements();
    Validate.isTrue(s1 > 0, "Value delta vector contained no data");
    final DoubleMatrix1D mean = data.getMean();
    final int s2 = mean.getNumberOfElements();
    Validate.isTrue(s1 > 0, "Mean vector contained no data");
    Validate.isTrue(s1 == s2, "Value delta and mean vectors were of different size");
    return _algebra.getInnerProduct(delta, mean);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_algebra == null) ? 0 : _algebra.hashCode());
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
    final DeltaMeanCalculator other = (DeltaMeanCalculator) obj;
    return ObjectUtils.equals(_algebra, other._algebra);
  }

}
