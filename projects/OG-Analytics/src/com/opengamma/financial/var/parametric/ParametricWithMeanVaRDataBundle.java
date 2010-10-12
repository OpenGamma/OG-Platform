/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricWithMeanVaRDataBundle extends ParametricVaRDataBundle {
  private final DoubleMatrix1D _mean;

  public ParametricWithMeanVaRDataBundle(final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order, final DoubleMatrix1D mean) {
    super(sensitivities, covarianceMatrix, order);
    Validate.notNull(mean, "mean");
    testData(sensitivities, mean);
    _mean = mean;
  }

  public ParametricWithMeanVaRDataBundle(final List<String> names, final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order, final DoubleMatrix1D mean) {
    super(names, sensitivities, covarianceMatrix, order);
    Validate.notNull(mean, "mean");
    testData(sensitivities, mean);
    _mean = mean;
  }

  public DoubleMatrix1D getMean() {
    return _mean;
  }

  private void testData(final Matrix<?> sensitivities, final DoubleMatrix1D mean) {
    if (sensitivities instanceof DoubleMatrix1D) {
      Validate.isTrue(sensitivities.getNumberOfElements() == mean.getNumberOfElements());
    } else {
      Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == mean.getNumberOfElements());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_mean == null) ? 0 : _mean.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ParametricWithMeanVaRDataBundle other = (ParametricWithMeanVaRDataBundle) obj;
    return ObjectUtils.equals(_mean, other._mean);
  }

}
