/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.Map;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import com.opengamma.financial.sensitivity.Sensitivity;

/**
 * @author emcleod
 * 
 */
public class ParametricWithMeanVaRDataBundle extends ParametricVaRDataBundle {
  private final Map<Sensitivity, DoubleMatrix1D> _mean;

  public ParametricWithMeanVaRDataBundle(final Map<Sensitivity, DoubleMatrix1D> mean, final Map<Sensitivity, DoubleMatrix1D> sensitivities,
      final Map<Sensitivity, DoubleMatrix2D> covariances) {
    super(sensitivities, covariances);
    if (mean == null)
      throw new IllegalArgumentException("Mean map was null");
    _mean = mean;
  }

  public DoubleMatrix1D getMean(final Sensitivity greek) {
    if (!_mean.containsKey(greek))
      throw new IllegalArgumentException("Map does not contain vector for " + greek);
    return _mean.get(greek);
  }
}
