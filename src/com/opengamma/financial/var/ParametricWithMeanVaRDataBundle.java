/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.Map;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import com.opengamma.financial.greeks.value.ValueGreek;

/**
 * @author emcleod
 * 
 */
public class ParametricWithMeanVaRDataBundle extends ParametricVaRDataBundle {
  private final Map<ValueGreek, DoubleMatrix1D> _mean;

  public ParametricWithMeanVaRDataBundle(final Map<ValueGreek, DoubleMatrix1D> mean, final Map<ValueGreek, DoubleMatrix1D> sensitivities,
      final Map<ValueGreek, DoubleMatrix2D> covariances) {
    super(sensitivities, covariances);
    if (mean == null)
      throw new IllegalArgumentException("Mean map was null");
    _mean = mean;
  }

  public DoubleMatrix1D getMean(final ValueGreek greek) {
    if (!_mean.containsKey(greek))
      throw new IllegalArgumentException("Map does not contain vector for " + greek);
    return _mean.get(greek);
  }
}
