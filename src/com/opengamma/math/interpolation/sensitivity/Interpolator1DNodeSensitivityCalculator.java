/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;

/**
 * 
 * @param <T> Type of the data bundle
 */
public interface Interpolator1DNodeSensitivityCalculator<T extends Interpolator1DDataBundle> {

  double[] calculate(Interpolator1D<T, InterpolationResult> interpolator, T data, Double value);
}
