/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.Map;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricWithMeanVaRDataBundle extends ParametricVaRDataBundle {
  private final Map<Integer, DoubleMatrix1D> _mean;

  public ParametricWithMeanVaRDataBundle(final Map<Integer, DoubleMatrix1D> mean, final Map<Integer, Matrix<?>> sensitivities, final Map<Integer, DoubleMatrix2D> covariances) {
    super(sensitivities, covariances);
    testData(sensitivities, mean);
    _mean = mean;
  }

  public DoubleMatrix1D getMean(final int order) {
    return _mean.get(order);
  }

  private void testData(final Map<Integer, Matrix<?>> sensitivities, final Map<Integer, DoubleMatrix1D> mean) {
    if (mean == null)
      throw new IllegalArgumentException("Mean data were null");
    Matrix<?> m;
    for (final Integer order : sensitivities.keySet()) {
      m = sensitivities.get(order);
      if (mean.containsKey(order)) {
        if (m instanceof DoubleMatrix1D) {
          if (((DoubleMatrix1D) m).getNumberOfElements() != mean.get(order).getNumberOfElements())
            throw new IllegalArgumentException("Mean and sensitivity vector sizes were not equal");
        } else if (m instanceof DoubleMatrix2D) {
          if (((DoubleMatrix2D) m).getNumberOfColumns() != mean.get(order).getNumberOfElements())
            throw new IllegalArgumentException("Mean and sensitivity matrix sizes were not equal");
        }
      }
    }
  }
}
