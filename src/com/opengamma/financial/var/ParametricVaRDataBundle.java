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
public class ParametricVaRDataBundle {
  private final Map<ValueGreek, DoubleMatrix1D> _sensitivities;
  private final Map<ValueGreek, DoubleMatrix2D> _covariances;

  public ParametricVaRDataBundle(final Map<ValueGreek, DoubleMatrix1D> sensitivities, final Map<ValueGreek, DoubleMatrix2D> covariances) {
    if (sensitivities == null)
      throw new IllegalArgumentException("Sensitivities map was null");
    if (covariances == null)
      throw new IllegalArgumentException("Covariance map was null");
    _sensitivities = sensitivities;
    _covariances = covariances;
  }

  public DoubleMatrix1D getValueGreekVector(final ValueGreek greek) {
    if (!_sensitivities.containsKey(greek))
      throw new IllegalArgumentException("Map does not contain vector for " + greek);
    return _sensitivities.get(greek);
  }

  public DoubleMatrix2D getCovarianceMatrix(final ValueGreek greek) {
    if (!_covariances.containsKey(greek))
      throw new IllegalArgumentException("Map does not contain matrix for " + greek);
    return _covariances.get(greek);
  }

}
