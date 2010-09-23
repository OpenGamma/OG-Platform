/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaMeanCalculator extends Function1D<ParametricWithMeanVaRDataBundle, Double> {
  private final MatrixAlgebra _algebra;
  private static final int FIRST_ORDER = 1;

  public DeltaMeanCalculator(final MatrixAlgebra algebra) {
    Validate.notNull(algebra, "algebra");
    _algebra = algebra;
  }

  @Override
  public Double evaluate(final ParametricWithMeanVaRDataBundle data) {
    Validate.notNull(data, "data");
    final DoubleMatrix1D delta = (DoubleMatrix1D) data.getSensitivityData(FIRST_ORDER);
    final int s1 = delta.getNumberOfElements();
    if (s1 == 0) {
      throw new IllegalArgumentException("Value delta vector contained no data");
    }
    final DoubleMatrix1D mean = data.getMean(FIRST_ORDER);
    final int s2 = mean.getNumberOfElements();
    if (s2 == 0) {
      throw new IllegalArgumentException("Mean vector contained no data");
    }
    if (s1 != s2) {
      throw new IllegalArgumentException("Value delta and mean vectors were of different size");
    }
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
    DeltaMeanCalculator other = (DeltaMeanCalculator) obj;
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
