/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.Map;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * @author emcleod
 * 
 */
public class ParametricWithMeanVaRDataBundle extends ParametricVaRDataBundle {
  private final Map<Sensitivity, DoubleMatrix1D> _mean;

  public ParametricWithMeanVaRDataBundle(final Map<Sensitivity, DoubleMatrix1D> mean, final Map<Sensitivity, Matrix<?>> sensitivities,
      final Map<Sensitivity, DoubleMatrix2D> covariances) {
    super(sensitivities, covariances);
    testData(sensitivities, mean);
    _mean = mean;
  }

  public DoubleMatrix1D getMean(final Sensitivity greek) {
    return _mean.get(greek);
  }

  private void testData(final Map<Sensitivity, Matrix<?>> sensitivities, final Map<Sensitivity, DoubleMatrix1D> mean) {
    if (mean == null)
      throw new IllegalArgumentException("Mean data were null");
    Matrix<?> m;
    for (final Sensitivity s : sensitivities.keySet()) {
      m = sensitivities.get(s);
      if (mean.containsKey(s)) {
        if (m instanceof DoubleMatrix1D) {
          if (((DoubleMatrix1D) m).getNumberOfElements() != mean.get(s).getNumberOfElements())
            throw new IllegalArgumentException("Mean and sensitivity vector sizes were not equal");
        } else if (m instanceof DoubleMatrix2D) {
          if (((DoubleMatrix2D) m).getNumberOfColumns() != mean.get(s).getNumberOfElements())
            throw new IllegalArgumentException("Mean and sensitivity matrix sizes were not equal");
        }
      }
    }
  }
}
