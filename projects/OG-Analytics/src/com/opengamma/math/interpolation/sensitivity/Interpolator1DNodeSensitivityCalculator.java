/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * @param <T> Type of the data bundle
 */
public interface Interpolator1DNodeSensitivityCalculator<T extends Interpolator1DDataBundle> {

  double[] calculate(T data, double value);

}
